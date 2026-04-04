package com.navy.casualty.admin.service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;

import com.navy.casualty.admin.dto.ImportResultResponse;
import com.navy.casualty.admin.dto.ImportResultResponse.ImportError;
import com.navy.casualty.dead.dto.DeadCreateRequest;
import com.navy.casualty.dead.service.DeadService;
import com.navy.casualty.review.dto.ReviewCreateRequest;
import com.navy.casualty.review.entity.ReviewClassification;
import com.navy.casualty.review.service.ReviewService;
import com.navy.casualty.wounded.dto.WoundedCreateRequest;
import com.navy.casualty.wounded.entity.WoundType;
import com.navy.casualty.wounded.service.WoundedService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

/**
 * Excel 파일 임포트 서비스.
 * 사망자/상이자/전공사상심사 3종 데이터를 Excel 파일에서 일괄 등록한다.
 * 행별 검증 후 유효한 행만 DB에 저장하고, 오류 행은 상세 사유와 함께 반환한다.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DataImportService {

    private static final int MAX_ROWS = 10_000;
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    private final DeadService deadService;
    private final WoundedService woundedService;
    private final ReviewService reviewService;

    /**
     * Excel 파일을 읽어 지정 타입의 데이터를 일괄 임포트한다.
     *
     * @param type 임포트 타입 (dead, wounded, review)
     * @param file Excel 파일 (.xlsx)
     * @return 임포트 결과 (성공/실패 건수 + 오류 상세)
     */
    public ImportResultResponse importExcel(String type, MultipartFile file) {
        validateType(type);

        try (Workbook workbook = new XSSFWorkbook(file.getInputStream())) {
            Sheet sheet = workbook.getSheetAt(0);
            int lastRow = sheet.getLastRowNum();

            if (lastRow < 1) {
                return new ImportResultResponse(0, 0, 0, List.of());
            }

            if (lastRow > MAX_ROWS) {
                throw new IllegalArgumentException(
                        "최대 " + MAX_ROWS + "행까지 임포트 가능합니다 (현재: " + lastRow + "행)");
            }

            List<ImportError> errors = new ArrayList<>();
            int successCount = 0;

            for (int rowIdx = 1; rowIdx <= lastRow; rowIdx++) {
                Row row = sheet.getRow(rowIdx);
                if (row == null || isEmptyRow(row)) {
                    continue;
                }

                try {
                    List<ImportError> rowErrors = switch (type) {
                        case "dead" -> importDeadRow(row, rowIdx);
                        case "wounded" -> importWoundedRow(row, rowIdx);
                        case "review" -> importReviewRow(row, rowIdx);
                        default -> throw new IllegalArgumentException("지원하지 않는 임포트 타입: " + type);
                    };

                    if (rowErrors.isEmpty()) {
                        successCount++;
                    } else {
                        errors.addAll(rowErrors);
                    }
                } catch (Exception e) {
                    errors.add(new ImportError(rowIdx + 1, "-", "처리 오류: " + e.getMessage()));
                }
            }

            int totalRows = lastRow;
            int errorRows = (int) errors.stream().mapToInt(ImportError::rowNumber).distinct().count();

            return new ImportResultResponse(totalRows, successCount, errorRows, errors);
        } catch (IllegalArgumentException e) {
            throw e;
        } catch (IOException e) {
            throw new IllegalStateException("Excel 파일 읽기 실패", e);
        }
    }

    /**
     * 오류 목록을 Excel 파일로 생성한다.
     *
     * @param errors 오류 목록
     * @return Excel 파일 바이트 배열
     */
    public byte[] generateErrorReport(List<ImportError> errors) {
        try (SXSSFWorkbook workbook = new SXSSFWorkbook(100)) {
            Sheet sheet = workbook.createSheet("임포트 오류");

            // 헤더 생성
            Row headerRow = sheet.createRow(0);
            CellStyle headerStyle = createHeaderStyle(workbook);

            String[] headers = {"행번호", "컬럼", "사유"};
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }

            // 데이터 행 생성
            for (int i = 0; i < errors.size(); i++) {
                ImportError error = errors.get(i);
                Row row = sheet.createRow(i + 1);
                row.createCell(0).setCellValue(error.rowNumber());
                row.createCell(1).setCellValue(error.column());
                row.createCell(2).setCellValue(error.reason());
            }

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            workbook.write(out);
            return out.toByteArray();
        } catch (IOException e) {
            throw new IllegalStateException("오류 리포트 생성 실패", e);
        }
    }

    /**
     * 사망자 행을 검증하고 등록한다.
     * 컬럼 순서: 군번, 성명, 주민번호, 생년월일, 계급ID, 군구분ID, 소속ID,
     *           입대일자, 전화번호, 사망구분ID, 사망코드ID, 주소, 사망일자
     */
    private List<ImportError> importDeadRow(Row row, int rowIdx) {
        int displayRow = rowIdx + 1;
        List<ImportError> errors = new ArrayList<>();

        String serviceNumber = getCellString(row, 0);
        String name = getCellString(row, 1);
        String ssn = getCellString(row, 2);
        String birthDateStr = getCellString(row, 3);
        String rankIdStr = getCellString(row, 4);
        String branchIdStr = getCellString(row, 5);
        String unitIdStr = getCellString(row, 6);
        String enlistDateStr = getCellString(row, 7);
        String phone = getCellString(row, 8);
        String deathTypeIdStr = getCellString(row, 9);
        String deathCodeIdStr = getCellString(row, 10);
        String address = getCellString(row, 11);
        String deathDateStr = getCellString(row, 12);

        // 필수 필드 검증
        if (isBlank(serviceNumber)) errors.add(new ImportError(displayRow, "군번", "필수 값 누락"));
        if (isBlank(name)) errors.add(new ImportError(displayRow, "성명", "필수 값 누락"));
        if (isBlank(ssn)) {
            errors.add(new ImportError(displayRow, "주민번호", "필수 값 누락"));
        } else if (!isValidRrn(ssn)) {
            errors.add(new ImportError(displayRow, "주민번호", "형식 오류 (13자리 체크섬 불일치)"));
        }
        if (isBlank(deathDateStr)) errors.add(new ImportError(displayRow, "사망일자", "필수 값 누락"));

        if (!errors.isEmpty()) {
            return errors;
        }

        try {
            LocalDate birthDate = isBlank(birthDateStr) ? null : LocalDate.parse(birthDateStr, DATE_FORMAT);
            LocalDate enlistDate = isBlank(enlistDateStr) ? null : LocalDate.parse(enlistDateStr, DATE_FORMAT);
            LocalDate deathDate = LocalDate.parse(deathDateStr, DATE_FORMAT);

            DeadCreateRequest request = new DeadCreateRequest(
                    serviceNumber, name, ssn, birthDate,
                    parseLongOrNull(rankIdStr),
                    parseLongOrNull(branchIdStr),
                    parseLongOrNull(unitIdStr),
                    enlistDate, phone,
                    parseLongOrNull(deathTypeIdStr),
                    parseLongOrNull(deathCodeIdStr),
                    address, deathDate
            );
            deadService.create(request);
        } catch (DateTimeParseException e) {
            errors.add(new ImportError(displayRow, "날짜", "날짜 형식 오류 (yyyy-MM-dd)"));
        } catch (IllegalArgumentException e) {
            errors.add(new ImportError(displayRow, "-", e.getMessage()));
        }

        return errors;
    }

    /**
     * 상이자 행을 검증하고 등록한다.
     * 컬럼 순서: 군번, 성명, 주민번호, 생년월일, 계급ID, 군구분ID, 소속ID,
     *           입대일자, 전화번호, 주소, 보훈청ID, 병명, 상이구분
     */
    private List<ImportError> importWoundedRow(Row row, int rowIdx) {
        int displayRow = rowIdx + 1;
        List<ImportError> errors = new ArrayList<>();

        String serviceNumber = getCellString(row, 0);
        String name = getCellString(row, 1);
        String ssn = getCellString(row, 2);
        String birthDateStr = getCellString(row, 3);
        String rankIdStr = getCellString(row, 4);
        String branchIdStr = getCellString(row, 5);
        String unitIdStr = getCellString(row, 6);
        String enlistDateStr = getCellString(row, 7);
        String phone = getCellString(row, 8);
        String address = getCellString(row, 9);
        String veteransOfficeIdStr = getCellString(row, 10);
        String diseaseName = getCellString(row, 11);
        String woundTypeStr = getCellString(row, 12);

        // 필수 필드 검증
        if (isBlank(serviceNumber)) errors.add(new ImportError(displayRow, "군번", "필수 값 누락"));
        if (isBlank(name)) errors.add(new ImportError(displayRow, "성명", "필수 값 누락"));
        if (isBlank(ssn)) {
            errors.add(new ImportError(displayRow, "주민번호", "필수 값 누락"));
        } else if (!isValidRrn(ssn)) {
            errors.add(new ImportError(displayRow, "주민번호", "형식 오류 (13자리 체크섬 불일치)"));
        }
        if (isBlank(woundTypeStr)) errors.add(new ImportError(displayRow, "상이구분", "필수 값 누락"));

        if (!errors.isEmpty()) {
            return errors;
        }

        try {
            LocalDate birthDate = isBlank(birthDateStr) ? null : LocalDate.parse(birthDateStr, DATE_FORMAT);
            LocalDate enlistDate = isBlank(enlistDateStr) ? null : LocalDate.parse(enlistDateStr, DATE_FORMAT);
            WoundType woundType = WoundType.valueOf(woundTypeStr.toUpperCase());

            WoundedCreateRequest request = new WoundedCreateRequest(
                    serviceNumber, name, ssn, birthDate,
                    parseLongOrNull(rankIdStr),
                    parseLongOrNull(branchIdStr),
                    parseLongOrNull(unitIdStr),
                    enlistDate, phone, address,
                    parseLongOrNull(veteransOfficeIdStr),
                    diseaseName, woundType
            );
            woundedService.create(request);
        } catch (DateTimeParseException e) {
            errors.add(new ImportError(displayRow, "날짜", "날짜 형식 오류 (yyyy-MM-dd)"));
        } catch (IllegalArgumentException e) {
            errors.add(new ImportError(displayRow, "-", e.getMessage()));
        }

        return errors;
    }

    /**
     * 심사 행을 검증하고 등록한다.
     * 컬럼 순서: 심사차수, 심사일자, 성명, 군번, 주민번호, 생년월일,
     *           계급ID, 군구분ID, 소속ID, 입대일자, 병명, 소속부대심사결과, 분류
     */
    private List<ImportError> importReviewRow(Row row, int rowIdx) {
        int displayRow = rowIdx + 1;
        List<ImportError> errors = new ArrayList<>();

        String reviewRoundStr = getCellString(row, 0);
        String reviewDateStr = getCellString(row, 1);
        String name = getCellString(row, 2);
        String serviceNumber = getCellString(row, 3);
        String ssn = getCellString(row, 4);
        String birthDateStr = getCellString(row, 5);
        String rankIdStr = getCellString(row, 6);
        String branchIdStr = getCellString(row, 7);
        String unitIdStr = getCellString(row, 8);
        String enlistDateStr = getCellString(row, 9);
        String diseaseName = getCellString(row, 10);
        String unitReviewResult = getCellString(row, 11);
        String classificationStr = getCellString(row, 12);

        // 필수 필드 검증
        if (isBlank(reviewRoundStr)) errors.add(new ImportError(displayRow, "심사차수", "필수 값 누락"));
        if (isBlank(name)) errors.add(new ImportError(displayRow, "성명", "필수 값 누락"));
        if (isBlank(serviceNumber)) errors.add(new ImportError(displayRow, "군번", "필수 값 누락"));
        // 주민번호 검증 (선택이지만 입력 시 형식 확인)
        if (!isBlank(ssn) && !isValidRrn(ssn)) {
            errors.add(new ImportError(displayRow, "주민번호", "형식 오류 (13자리 체크섬 불일치)"));
        }

        if (!errors.isEmpty()) {
            return errors;
        }

        try {
            Integer reviewRound = Integer.parseInt(reviewRoundStr.replaceAll("\\.0$", ""));
            LocalDate reviewDate = isBlank(reviewDateStr) ? null : LocalDate.parse(reviewDateStr, DATE_FORMAT);
            LocalDate birthDate = isBlank(birthDateStr) ? null : LocalDate.parse(birthDateStr, DATE_FORMAT);
            LocalDate enlistDate = isBlank(enlistDateStr) ? null : LocalDate.parse(enlistDateStr, DATE_FORMAT);
            ReviewClassification classification = isBlank(classificationStr)
                    ? null : ReviewClassification.valueOf(classificationStr.toUpperCase());

            ReviewCreateRequest request = new ReviewCreateRequest(
                    reviewRound, reviewDate, name, serviceNumber,
                    isBlank(ssn) ? null : ssn,
                    birthDate,
                    parseLongOrNull(rankIdStr),
                    parseLongOrNull(branchIdStr),
                    parseLongOrNull(unitIdStr),
                    enlistDate, diseaseName, unitReviewResult, classification
            );
            reviewService.create(request);
        } catch (DateTimeParseException e) {
            errors.add(new ImportError(displayRow, "날짜", "날짜 형식 오류 (yyyy-MM-dd)"));
        } catch (NumberFormatException e) {
            errors.add(new ImportError(displayRow, "심사차수", "숫자 형식 오류"));
        } catch (IllegalArgumentException e) {
            errors.add(new ImportError(displayRow, "-", e.getMessage()));
        }

        return errors;
    }

    /**
     * 주민등록번호 체크섬을 검증한다 (13자리, 마지막 자리 체크섬).
     */
    private boolean isValidRrn(String rrn) {
        if (rrn == null || rrn.length() != 13) {
            return false;
        }
        // 숫자만 허용
        if (!rrn.matches("\\d{13}")) {
            return false;
        }
        int[] weights = {2, 3, 4, 5, 6, 7, 8, 9, 2, 3, 4, 5};
        int sum = 0;
        for (int i = 0; i < 12; i++) {
            sum += (rrn.charAt(i) - '0') * weights[i];
        }
        int checkDigit = (11 - (sum % 11)) % 10;
        return checkDigit == (rrn.charAt(12) - '0');
    }

    /**
     * 임포트 타입이 유효한지 검증한다.
     */
    private void validateType(String type) {
        if (type == null || !List.of("dead", "wounded", "review").contains(type)) {
            throw new IllegalArgumentException("지원하지 않는 임포트 타입: " + type);
        }
    }

    /**
     * 셀 값을 문자열로 읽는다.
     */
    private String getCellString(Row row, int colIdx) {
        Cell cell = row.getCell(colIdx);
        if (cell == null) {
            return null;
        }
        return switch (cell.getCellType()) {
            case STRING -> cell.getStringCellValue().trim();
            case NUMERIC -> {
                if (DateUtil.isCellDateFormatted(cell)) {
                    yield cell.getLocalDateTimeCellValue().toLocalDate().format(DATE_FORMAT);
                }
                double val = cell.getNumericCellValue();
                if (val == Math.floor(val) && !Double.isInfinite(val)) {
                    yield String.valueOf((long) val);
                }
                yield String.valueOf(val);
            }
            case BOOLEAN -> String.valueOf(cell.getBooleanCellValue());
            case FORMULA -> cell.getStringCellValue().trim();
            default -> null;
        };
    }

    /**
     * 행이 비어있는지 확인한다.
     */
    private boolean isEmptyRow(Row row) {
        for (int i = row.getFirstCellNum(); i < row.getLastCellNum(); i++) {
            Cell cell = row.getCell(i);
            if (cell != null && cell.getCellType() != CellType.BLANK) {
                String value = getCellString(row, i);
                if (!isBlank(value)) {
                    return false;
                }
            }
        }
        return true;
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    private Long parseLongOrNull(String value) {
        if (isBlank(value)) {
            return null;
        }
        try {
            return Long.parseLong(value.replaceAll("\\.0$", ""));
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /**
     * 헤더 셀 스타일을 생성한다 (굵은 글씨 + 회색 배경).
     */
    private CellStyle createHeaderStyle(SXSSFWorkbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        style.setFont(font);
        style.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        return style;
    }
}
