package com.theatermgnt.theatermgnt.staff.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.theatermgnt.theatermgnt.staff.entity.Staff;

@Repository
public interface StaffRepository extends JpaRepository<Staff, String> {
    Optional<Staff> findByAccountId(String accountId);

    List<Staff> findAllByFirstNameContainingIgnoreCaseOrLastNameContainingIgnoreCase(String firstName, String lastName);

    @Query(
            """
		SELECT DISTINCT s
		FROM Staff s
		JOIN s.roles r
		WHERE s.cinemaId = :cinemaId
		AND r.name = :roleName
	""")
    List<Staff> findByCinemaIdAndRole(@Param("cinemaId") String cinemaId, @Param("roleName") String roleName);

    @Query(
            """
		SELECT DISTINCT s
		FROM Staff s
		JOIN s.roles r
		WHERE r.name = :roleName
		AND s.id NOT IN (
			SELECT c.manager.id
			FROM Cinema c
			WHERE c.manager IS NOT NULL
		)
	""")
    List<Staff> findByRoleAndNotManagingAnyCinema(@Param("roleName") String roleName);

    @Query("""
		SELECT COUNT(s) > 0
		FROM Staff s
		JOIN s.roles r
		WHERE r.name = :roleName
	""")
    boolean existsByRoleName(@Param("roleName") String roleName);
}
