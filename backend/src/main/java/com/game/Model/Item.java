package com.game.Model;

import lombok.Data;
import javax.persistence.*;

@Data
@Entity
@Table(name = "item")
public class Item {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private String type;
    private String mota;
    private String thuoctinh;
    private String icon;
}