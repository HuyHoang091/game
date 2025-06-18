package com.game.Repository;

import com.game.Model.ThuocTinh;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ThuocTinhRepository extends JpaRepository<ThuocTinh, Long> {
    // List<ThuocTinh> findById(Long id);
}