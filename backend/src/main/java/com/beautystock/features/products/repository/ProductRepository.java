package com.beautystock.features.products.repository;

import com.beautystock.features.products.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface ProductRepository extends JpaRepository<Product, Long> {
  List<Product> findByOwnerEmailOrderByCreatedAtDesc(String ownerEmail);
  Optional<Product> findByIdAndOwnerEmail(Long id, String ownerEmail);

  List<Product> findByOwnerEmailAndCategoryIgnoreCaseOrderByCreatedAtDesc(String ownerEmail, String category);

  List<Product> findByOwnerEmailAndExpirationDateBetweenOrderByExpirationDateAsc(String ownerEmail, LocalDate start, LocalDate end);

  long countByOwnerEmail(String ownerEmail);
  long countByOwnerEmailAndExpirationDateBefore(String ownerEmail, LocalDate date);

  @Query("select count(p) from Product p where p.ownerEmail = :ownerEmail and lower(coalesce(p.status, '')) like '%running%'")
  long countRunningLowByOwnerEmail(@Param("ownerEmail") String ownerEmail);

    @Query("""
           select p from Product p
           where p.ownerEmail = :ownerEmail
             and (
               lower(p.name) like lower(concat('%', :query, '%'))
               or lower(p.brand) like lower(concat('%', :query, '%'))
               or lower(p.category) like lower(concat('%', :query, '%'))
               or lower(coalesce(p.description, '')) like lower(concat('%', :query, '%'))
             )
           order by p.createdAt desc
           """)
    List<Product> search(@Param("ownerEmail") String ownerEmail, @Param("query") String query);

    @Query("select coalesce(sum(p.price), 0) from Product p where p.ownerEmail = :ownerEmail")
    Double sumPriceByOwner(@Param("ownerEmail") String ownerEmail);
}
