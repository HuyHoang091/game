package com.game.service;

import com.game.entity.ThuocTinh;
import com.game.repository.ThuocTinhRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class ThuocTinhService {
    @Autowired
    private ThuocTinhRepository characterRepository;

    public List<ThuocTinh> getAllThuocTinh() {
        return characterRepository.findAll();
    }
}