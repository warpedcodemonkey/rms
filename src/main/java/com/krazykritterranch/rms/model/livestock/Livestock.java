package com.krazykritterranch.rms.model.livestock;

import com.krazykritterranch.rms.model.BaseVO;
import com.krazykritterranch.rms.model.common.Account;
import com.krazykritterranch.rms.model.common.Address;
import com.krazykritterranch.rms.model.common.Note;
import com.krazykritterranch.rms.model.common.Weight;

import jakarta.persistence.*;
import java.sql.Date;
import java.util.ArrayList;
import java.util.List;
import java.util.StringJoiner;

@Entity
@AttributeOverride(name="id", column = @Column(name="livestock_id"))
public class Livestock extends BaseVO {

    private String tagId;

    // Add account relationship for multi-tenancy
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "account_id", nullable = false)
    private Account account;

    @OneToOne
    @JoinColumn(name = "livestock_type_id")
    private LivestockType livestockType;

    @OneToOne
    @JoinColumn(name = "breed_id")
    private Breed breed;

    private Date dateOfBirth;

    @ManyToOne
    @JoinColumn(name = "mother_id")
    private Livestock mother;

    @ManyToOne
    @JoinColumn(name = "father_id")
    private Livestock father;

    @ManyToMany
    @JoinTable(
            name = "livestock_vaccinations",
            joinColumns = @JoinColumn(name = "livestock_id"),
            inverseJoinColumns = @JoinColumn(name = "vac_id")
    )
    private List<Vaccination> vaccinations = new ArrayList<>();

    private Date dateBred;

    @ManyToMany
    @JoinTable(
            name = "livestock_weights",
            joinColumns = @JoinColumn(name = "livestock_id"),
            inverseJoinColumns = @JoinColumn(name = "weight_id")
    )
    private List<Weight> weights = new ArrayList<>();

    private String name;

    @OneToOne
    @JoinColumn(name ="address_id")
    private Address location;

    @ManyToMany
    @JoinTable(
            name = "livestock_feedings",
            joinColumns = @JoinColumn(name = "livestock_id"),
            inverseJoinColumns = @JoinColumn(name = "feeding_id")
    )
    private List<Feeding> feedings = new ArrayList<>();

    @ManyToMany
    @JoinTable(
            name = "livestock_notes",
            joinColumns = @JoinColumn(name = "livestock_id"),
            inverseJoinColumns = @JoinColumn(name = "note_id")
    )
    private List<Note> notes = new ArrayList<>();

    @ManyToMany
    @JoinTable(
            name = "livestock_heatcylces",
            joinColumns = @JoinColumn(name = "livestock_id"),
            inverseJoinColumns = @JoinColumn(name = "hc_id")
    )
    private List<HeatCycle> heatCycles = new ArrayList<>();

    private String description;

    // Getters and Setters
    public String getTagId() { return tagId; }
    public void setTagId(String tagId) { this.tagId = tagId; }

    public Account getAccount() { return account; }
    public void setAccount(Account account) { this.account = account; }

    public LivestockType getLivestockType() { return livestockType; }
    public void setLivestockType(LivestockType livestockType) { this.livestockType = livestockType; }

    public Breed getBreed() { return breed; }
    public void setBreed(Breed breed) { this.breed = breed; }

    public Date getDateOfBirth() { return dateOfBirth; }
    public void setDateOfBirth(Date dateOfBirth) { this.dateOfBirth = dateOfBirth; }

    public Livestock getMother() { return mother; }
    public void setMother(Livestock mother) { this.mother = mother; }

    public Livestock getFather() { return father; }
    public void setFather(Livestock father) { this.father = father; }

    public List<Vaccination> getVaccinations() { return vaccinations; }
    public void setVaccinations(List<Vaccination> vaccinations) { this.vaccinations = vaccinations; }

    public Date getDateBred() { return dateBred; }
    public void setDateBred(Date dateBred) { this.dateBred = dateBred; }

    public List<Weight> getWeights() { return weights; }
    public void setWeights(List<Weight> weights) { this.weights = weights; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public Address getLocation() { return location; }
    public void setLocation(Address location) { this.location = location; }

    public List<Feeding> getFeedings() { return feedings; }
    public void setFeedings(List<Feeding> feedings) { this.feedings = feedings; }

    public List<Note> getNotes() { return notes; }
    public void setNotes(List<Note> notes) { this.notes = notes; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public List<HeatCycle> getHeatCycles() { return heatCycles; }
    public void setHeatCycles(List<HeatCycle> heatCycles) { this.heatCycles = heatCycles; }

    @Override
    public String toString() {
        return new StringJoiner(", ", Livestock.class.getSimpleName() + "[", "]")
                .add("tagId='" + tagId + "'")
                .add("account=" + (account != null ? account.getAccountNumber() : "null"))
                .add("livestockType=" + livestockType)
                .add("breed=" + breed)
                .add("dateOfBirth=" + dateOfBirth)
                .add("name='" + name + "'")
                .add("description='" + description + "'")
                .toString();
    }
}