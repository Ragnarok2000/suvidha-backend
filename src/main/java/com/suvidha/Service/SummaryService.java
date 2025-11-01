package com.suvidha.Service;

import java.util.List;
import java.util.Optional;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import com.suvidha.Exception.SummaryNotFoundException;
import com.suvidha.Modal.Summary;
import com.suvidha.Repsoitory.SummaryRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class SummaryService implements IsummaryService {

    private final SummaryRepository summaryRepository;

    @Override
    public List<Summary> getAll() {
        return summaryRepository.findAll();
    }

    @Override
    @Async // ✅ Runs in background thread
    @CacheEvict(value = "summariesByUser", key = "#summary.user.username")
    public Summary save(Summary summary) {
        return summaryRepository.save(summary);
    }

    @Override
    @Cacheable(value = "summariesByUser", key = "#username", unless = "#result == null or #result.isEmpty()")
    public List<Summary> getSummariesByUsername(String username) throws SummaryNotFoundException {
        System.out.println("⏳ Redis NOT used — fetching from DB for: " + username);

        List<Summary> summaries = summaryRepository.findByUserUsername(username);
        if (summaries != null && !summaries.isEmpty()) {
            return summaries;
        } else {
            throw new SummaryNotFoundException("No summaries found for user: " + username);
        }
    }

    @Override
    @Async // ✅ Optional: make deletion async too
    @CacheEvict(value = "summariesByUser", key = "#username")
    public boolean deleteById(Long id, String username) {
        Optional<Summary> summaryOpt = summaryRepository.findById(id);
        if (summaryOpt.isPresent()) {
            Summary summary = summaryOpt.get();
            if (summary.getUser() != null && username.equals(summary.getUser().getUsername())) {
                summaryRepository.delete(summary);
                return true;
            }
        }
        return false;
    }
}