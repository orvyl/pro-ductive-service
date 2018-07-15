package com.aws.hack;

import com.aws.hack.model.Preference;
import com.aws.hack.model.Story;
import com.aws.hack.model.User;
import com.aws.hack.repository.PreferenceRepository;
import com.aws.hack.repository.StoryRepository;
import com.aws.hack.repository.UserRepository;
import lombok.Data;
import no.priv.garshol.duke.Comparator;
import no.priv.garshol.duke.comparators.Levenshtein;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.function.Supplier;

@SpringBootApplication
public class Application {

	public static void main(String[] args) {
		SpringApplication.run(Application.class, args);
	}

	@RestController
	public static class HomeController {

		@Autowired
		private UserRepository userRepository;

		@Autowired
		private StoryRepository storyRepository;

		@Autowired
		private PreferenceRepository preferenceRepository;

		@Autowired
		private StoryService storyService;

		@GetMapping("/preferences")
		public List<Preference> getPreferences() {
			return (List<Preference>) preferenceRepository.findAll();
		}

		@GetMapping("/stories")
		public List<Story> getStories() {
			return (List<Story>) storyRepository.findAll();
		}

		@GetMapping("/users")
		public List<User> getUsers() {
			return (List<User>) userRepository.findAll();
		}

		@GetMapping("/stories/{id}")
		public List<PreferredStory> getLoggedInUserStories(@PathVariable Long id) throws Throwable {
			Optional<User> u = userRepository.findById(id);
			User user = u.orElseThrow((Supplier<Throwable>) () -> new Exception("User not found!"));
			return storyService.getPreferredStories(user);
		}

		@GetMapping("/story/{id}")
		public Story getStory(@PathVariable Long id) throws Throwable {
			return storyRepository.findById(id).orElseThrow((Supplier<Throwable>) () -> new Exception("Story not found!"));
		}

		@GetMapping("/bookmark/{userId}/{storyId}")
		public void bookmark(@PathVariable Long userId, @PathVariable Long storyId) throws Throwable {
			storyService.bookmark(userRepository.findById(userId).orElseThrow((Supplier<Throwable>) () -> new Exception("User not found!")),
					storyRepository.findById(storyId).orElseThrow((Supplier<Throwable>) () -> new Exception("Story not found!")));
		}
	}

	@Service
	public static class StoryService {

		private static final Comparator COMPARATOR = new Levenshtein();

		@Autowired
		private UserRepository userRepository;

		@Autowired
		private StoryRepository storyRepository;

		public List<PreferredStory> getPreferredStories(User user) {
			List<PreferredStory> preferredStories = new ArrayList<>();
			List<Story> stories = (List<Story>) storyRepository.findAll();

			stories.stream()
					.filter(story -> getCommon(story.getTags(), user.getStringPreference()).size() > 0)
					.forEach(story -> {
						int preferenceMatchSize = getCommon(story.getTags(), user.getStringPreference()).size();
						int preferredTagMatchSize = getCommon(story.getTags(), user.getTags()).size();
						PreferredStory preferredStory = new PreferredStory((preferenceMatchSize * 95) + (preferredTagMatchSize * 5), story);
						preferredStories.add(preferredStory);
					});

			preferredStories.sort((o1, o2) -> o2.score - o1.score);

			return preferredStories;
		}

		public void bookmark(User user, Story story) {
			Set<String> tag = new HashSet<>();
			tag.addAll(user.getTags());
			tag.addAll(story.getTags());

			user.setTags(new ArrayList<>(tag));
			user.getBookmarks().add(story);
			userRepository.save(user);
		}

		private Set<String> getCommon(List<String> storyTags, List<String> preferences) {
			Set<String> storyTagsSet = new HashSet<>();
			for (String storyTag : storyTags) {
				for (String userPreference : preferences) {
					double comparisonResult = COMPARATOR.compare(storyTag, userPreference);
					if ((storyTags.contains(userPreference)) || userPreference.contains(storyTag) || comparisonResult > .7) {
						storyTagsSet.add(userPreference);
					}
				}
			}

			return storyTagsSet;
		}
	}

	@Data
	public static class PreferredStory {

		public PreferredStory() {
		}

		public PreferredStory(int score, Story story) {
			this.score = score;
			this.story = story;
		}

		private int score;
		private Story story;
	}
}
