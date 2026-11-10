package org.delcom.ifs25022pbo2026springbootcashflow.domain.repository;

import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

/**
 * Kontrak (port) penyimpanan generik untuk operasi CRUD dan pencarian.
 *
 * @param <T>  tipe entitas domain
 * @param <ID> tipe identifier entitas
 */
public interface IRepository<T, ID> {
    /** Mengambil semua entitas. */
    List<T> findAll();

    /** Mencari satu entitas berdasarkan ID. */
    Optional<T> findById(ID id);

    /** Menyimpan entitas. */
    T save(T entity);

    /** Menghapus entitas berdasarkan ID; true jika ada yang terhapus. */
    boolean deleteById(ID id);

    /** Menyimpan perubahan pada entitas yang sudah ada. */
    void update(T entity);

    /** Mencari entitas yang memenuhi predikat. */
    List<T> findBy(Predicate<T> predicate);
}
