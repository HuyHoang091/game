package com.game.Service;

import com.game.Model.ThuocTinh;
import com.game.Repository.ThuocTinhRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class ThuocTinhService {
    @Autowired
    private ThuocTinhRepository thuocTinhRepository;

    public List<ThuocTinh> getAllThuocTinh() {
        return thuocTinhRepository.findAll();
    }
}