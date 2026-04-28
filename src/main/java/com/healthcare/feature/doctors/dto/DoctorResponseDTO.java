package com.healthcare.feature.doctors.dto;

import com.healthcare.entity.Doctor;
import com.healthcare.enums.AvailabilityStatus;
import java.time.LocalDateTime;

public class DoctorResponseDTO {
    private Long id;
    private Long userId;
    private String name;
    private String mobileNumber;
    private String email;
    private String qualification;
    private String experience;
    private String bio;
    private AvailabilityStatus availabilityStatus;
    private Integer experienceYears;
    private Double consultationFee;
    private Boolean isVerified;
    private String specializationName;
    private Long specializationId;
    private String totalExperience;
    private LocalDateTime createdAt;

    public static DoctorResponseDTO from(Doctor doctor) {
        if (doctor == null) return null;
        DoctorResponseDTO dto = new DoctorResponseDTO();
        dto.setId(doctor.getId());
        if (doctor.getUser() != null) {
            dto.setUserId(doctor.getUser().getId());
            dto.setName(doctor.getUser().getName());
            dto.setMobileNumber(doctor.getUser().getMobileNumber());
            dto.setEmail(doctor.getUser().getEmail());
        }
        dto.setQualification(doctor.getQualification());
        dto.setExperience(doctor.getExperience());
        dto.setBio(doctor.getBio());
        dto.setAvailabilityStatus(doctor.getAvailabilityStatus());
        dto.setExperienceYears(doctor.getExperienceYears());
        dto.setConsultationFee(doctor.getConsultationFee());
        dto.setIsVerified(doctor.getIsVerified());
        dto.setTotalExperience(doctor.getTotalExperience());
        dto.setCreatedAt(doctor.getCreatedAt());
        if (doctor.getSpecialization() != null) {
            dto.setSpecializationId(doctor.getSpecialization().getId());
            dto.setSpecializationName(doctor.getSpecialization().getName());
        }
        return dto;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getMobileNumber() { return mobileNumber; }
    public void setMobileNumber(String mobileNumber) { this.mobileNumber = mobileNumber; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getQualification() { return qualification; }
    public void setQualification(String qualification) { this.qualification = qualification; }
    public String getExperience() { return experience; }
    public void setExperience(String experience) { this.experience = experience; }
    public String getBio() { return bio; }
    public void setBio(String bio) { this.bio = bio; }
    public AvailabilityStatus getAvailabilityStatus() { return availabilityStatus; }
    public void setAvailabilityStatus(AvailabilityStatus availabilityStatus) { this.availabilityStatus = availabilityStatus; }
    public Integer getExperienceYears() { return experienceYears; }
    public void setExperienceYears(Integer experienceYears) { this.experienceYears = experienceYears; }
    public Double getConsultationFee() { return consultationFee; }
    public void setConsultationFee(Double consultationFee) { this.consultationFee = consultationFee; }
    public Boolean getIsVerified() { return isVerified; }
    public void setIsVerified(Boolean isVerified) { this.isVerified = isVerified; }
    public String getSpecializationName() { return specializationName; }
    public void setSpecializationName(String specializationName) { this.specializationName = specializationName; }
    public Long getSpecializationId() { return specializationId; }
    public void setSpecializationId(Long specializationId) { this.specializationId = specializationId; }
    public String getTotalExperience() { return totalExperience; }
    public void setTotalExperience(String totalExperience) { this.totalExperience = totalExperience; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
