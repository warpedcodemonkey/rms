package com.krazykritterranch.rms.model.livestock;

import com.krazykritterranch.rms.model.BaseVO;

import jakarta.persistence.*;
import java.util.StringJoiner;

@Entity
@AttributeOverride(name = "id", column = @Column(name="breed_id"))
public class Breed extends BaseVO {
    private String breedName;
    @OneToOne
    @JoinColumn(name = "livestock_type_id")
    private LivestockType livestockType;

    public String getBreedName() {
        return breedName;
    }

    public void setBreedName(String breedName) {
        this.breedName = breedName;
    }

    public LivestockType getLivestockType() {
        return livestockType;
    }

    public void setLivestockType(LivestockType livestockType) {
        this.livestockType = livestockType;
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", Breed.class.getSimpleName() + "[", "]")
                .add("breedName='" + breedName + "'")
                .add("livestockType=" + livestockType)
                .toString();
    }
}
