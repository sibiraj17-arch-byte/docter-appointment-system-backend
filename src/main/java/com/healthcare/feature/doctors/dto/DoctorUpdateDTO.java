package com.healthcare.feature.doctors.dto;

import jakarta.validation.constraints.Size;

public class DoctorUpdateDTO {

    @Size(max = 200)
    private String qualification;

    @Size(max = 2000)
    private String experience;

    @Size(max = 500)
    private String bio;

    private Integer experienceYears;

    private Double consultationFee;

    private Long specializationId;

    @Size(max = 200)
    private String totalExperience;

    public String getQualification() { return qualification; }
    public void setQualification(String qualification) { this.qualification = qualification; }
    public String getExperience() { return experience; }
    public void setExperience(String experience) { this.experience = experience; }
    public String getBio() { return bio; }
    public void setBio(String bio) { this.bio = bio; }
    public Integer getExperienceYears() { return experienceYears; }
    public void setExperienceYears(Integer experienceYears) { this.experienceYears = experienceYears; }
    public Double getConsultationFee() { return consultationFee; }
    public void setConsultationFee(Double consultationFee) { this.consultationFee = consultationFee; }
    public Long getSpecializationId() { return specializationId; }
    public void setSpecializationId(Long specializationId) { this.specializationId = specializationId; }
    public String getTotalExperience() { return totalExperience; }
    public void setTotalExperience(String totalExperience) { this.totalExperience = totalExperience; }
}
