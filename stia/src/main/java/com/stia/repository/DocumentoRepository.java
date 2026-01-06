package com.stia.repository;

import com.stia.entity.DocumentoRag;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DocumentoRepository extends JpaRepository<DocumentoRag, Long> {
}