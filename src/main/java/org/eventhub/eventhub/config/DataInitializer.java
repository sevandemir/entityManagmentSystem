package org.eventhub.eventhub.config;

import lombok.RequiredArgsConstructor;
import org.eventhub.eventhub.entity.Category;
import org.eventhub.eventhub.repo.CategoryRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {
    private final CategoryRepository categoryRepository;

    @Override
    public void run(String... args) {
        // Eğer tabloda hiç kategori yoksa varsayılanları ekle
        if (categoryRepository.count() == 0) {
            LocalDateTime now = LocalDateTime.now();
            List<Category> defaultCategories = List.of(
                    new Category(null, "Müzik", "Konserler ve festivaller", "icons/music.png", now),
                    new Category(null, "Teknoloji", "Workshop ve konferanslar", "icons/tech.png", now.plusSeconds(1)),
                    new Category(null, "Spor", "Maçlar ve turnuvalar", "icons/sports.png", now.plusSeconds(2)),
                    new Category(null, "Sanat", "Tiyatro ve sergiler", "icons/art.png", now.plusSeconds(3))
            );
            categoryRepository.saveAll(defaultCategories);
            System.out.println("Sisteme varsayılan kategoriler eklendi.");
        }
    }
}
