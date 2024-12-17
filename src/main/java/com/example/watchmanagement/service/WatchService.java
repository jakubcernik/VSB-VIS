package com.example.watchmanagement.service;

import com.example.watchmanagement.model.Watch;
import com.example.watchmanagement.repository.WatchRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class WatchService {

    private final WatchRepository watchRepository;

    public WatchService(WatchRepository watchRepository) {
        this.watchRepository = watchRepository;
    }

    public List<Watch> findAll() {
        return watchRepository.findAll();
    }

    public Optional<Watch> findById(Long id) {
        return watchRepository.findById(id);
    }

    public void save(Watch watch) {
        watchRepository.save(watch);
    }

    public void updateStock(Long id, int newStock) {
        watchRepository.updateStock(id, newStock);
    }

    public void deleteById(Long id) {
        watchRepository.deleteById(id);
    }
}
