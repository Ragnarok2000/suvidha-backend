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
    @Async
    @CacheEvict(value = "summariesByUser", key = "#summary.user.username")
    public Summary save(Summary summary) {
        System.out.println("🔄 Cache evicted for user: " + summary.getUser().getUsername());
        return summaryRepository.save(summary);
    }

    @Override
    @Cacheable(value = "summariesByUser", key = "#username", unless = "#result == null or #result.isEmpty()")
    public List<Summary> getSummariesByUsername(String username) throws SummaryNotFoundException {
        // ⚠️ This log ONLY appears on CACHE MISS (when method actually executes)
        System.out.println("🔴 CACHE MISS - Fetching summaries from database for user: " + username);

        List<Summary> summaries = summaryRepository.findByUserUsername(username);
        if (summaries != null && !summaries.isEmpty()) {
            System.out.println("✅ Found " + summaries.size() + " summaries in database for user: " + username);
            return summaries;
        } else {
            System.out.println("⚠️ No summaries found in database for user: " + username);
            throw new SummaryNotFoundException("No summaries found for user: " + username);
        }
    }

    @Override
    @Async
    @CacheEvict(value = "summariesByUser", key = "#username")
    public boolean deleteById(Long id, String username) {
        System.out.println("🗑️ Deleting summary with ID: " + id + " for user: " + username);
        Optional<Summary> summaryOpt = summaryRepository.findById(id);
        if (summaryOpt.isPresent()) {
            Summary summary = summaryOpt.get();
            if (summary.getUser() != null && username.equals(summary.getUser().getUsername())) {
                summaryRepository.delete(summary);
                System.out.println("✅ Summary deleted and cache evicted for user: " + username);
                return true;
            }
        }
        System.out.println("⚠️ Summary not found or unauthorized deletion attempt for ID: " + id);
        return false;
    }
}
