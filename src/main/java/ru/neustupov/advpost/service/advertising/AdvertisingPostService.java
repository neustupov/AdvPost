package ru.neustupov.advpost.service.advertising;

import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class AdvertisingPostService {

    @Scheduled(cron = "0 0 8 * * *")
    public void start() {

    }
}
