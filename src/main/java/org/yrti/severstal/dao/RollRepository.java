package org.yrti.severstal.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.yrti.severstal.model.Roll;


public interface RollRepository extends JpaRepository<Roll, Long>, JpaSpecificationExecutor<Roll> {
}
