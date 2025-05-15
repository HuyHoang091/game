package com.game.repository;

import com.game.entity.ThuocTinh;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ThuocTinhRepository extends JpaRepository<ThuocTinh, Long> {
    // List<ThuocTinh> findById(Long id);
}