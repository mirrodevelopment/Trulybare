package com.trulybare.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "app_settings")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AppSetting {

    @Id
    @Column(name = "setting_key")
    private String key;

    @Column(name = "setting_value", columnDefinition = "TEXT")
    private String value;
}
