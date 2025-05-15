package com.game.entity;

import lombok.Data;
import javax.persistence.*;

@Data
@Entity
@Table(name = "thuoctinh")
public class ThuocTinh {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private Double atk;
    private Double def;
    private Double hp;
    private Double mp;
    private Double critRate;
    private Double critDmg;
}