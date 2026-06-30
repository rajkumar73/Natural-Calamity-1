package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "emergency_contacts")
data class EmergencyContact(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val nameMr: String,
    val nameEn: String,
    val phone: String,
    val phoneAlt: String = "",
    val category: String, // ADMIN, MEDICAL, POLICE, FIRE, UTILITY, RESCUE
    val designationMr: String,
    val designationEn: String,
    val villageOrAreaMr: String,
    val villageOrAreaEn: String,
    val isDefault: Boolean = false,
    val notes: String = "",
    val isPending: Boolean = false,
    val isLocal: Boolean = false,
    val isPendingDelete: Boolean = false,
    val updateForContactId: Int? = null
)
