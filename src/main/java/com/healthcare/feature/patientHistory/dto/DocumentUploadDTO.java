package com.healthcare.feature.patientHistory.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class DocumentUploadDTO {
    @NotBlank(message = "Document name is required")
    @Size(max = 300)
    private String documentName;

    @Size(max = 100)
    private String documentType;

    @Size(max = 500)
    private String fileUrl;

    @Size(max = 1000)
    private String description;

    public String getDocumentName() { return documentName; }
    public void setDocumentName(String documentName) { this.documentName = documentName; }
    public String getDocumentType() { return documentType; }
    public void setDocumentType(String documentType) { this.documentType = documentType; }
    public String getFileUrl() { return fileUrl; }
    public void setFileUrl(String fileUrl) { this.fileUrl = fileUrl; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
}
