package com.indona.invento.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PackingSubmissionResponseDTO {

    private String packingId;
    private List<LineItemResponse> lineItems;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class LineItemResponse {
        private Long id;
        private String soNumber;
        private String lineNumber;

        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd/MM/yyyy HH:mm", timezone = "Asia/Kolkata")
        private LocalDateTime createdAt;
    }
}
