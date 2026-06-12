package com.stress.repository;

import com.stress.entity.Dummy;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DummyRepository extends JpaRepository<Dummy, Long> {

    long countByProcessadoTrue();
}
