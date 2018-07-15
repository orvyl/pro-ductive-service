package com.aws.hack;

import com.amazonaws.auth.*;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.comprehend.AmazonComprehend;
import com.amazonaws.services.comprehend.AmazonComprehendClientBuilder;
import com.amazonaws.services.comprehend.model.*;
import com.amazonaws.services.rekognition.AmazonRekognition;
import com.amazonaws.services.rekognition.AmazonRekognitionClientBuilder;
import com.amazonaws.services.rekognition.model.DetectLabelsRequest;
import com.amazonaws.services.rekognition.model.DetectLabelsResult;
import com.amazonaws.services.rekognition.model.Image;
import com.amazonaws.services.rekognition.model.Label;
import com.amazonaws.util.IOUtils;
import com.aws.hack.model.Preference;
import com.aws.hack.model.Story;
import com.aws.hack.model.User;
import com.aws.hack.repository.PreferenceRepository;
import com.aws.hack.repository.StoryRepository;
import com.aws.hack.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Predicate;

/**
 * author: vyl
 * date: 14/07/2018
 */
@Configuration
public class Bootstrap {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private StoryRepository storyRepository;

    @Autowired
    private PreferenceRepository preferenceRepository;

    @Bean
    public CommandLineRunner load() {
        return args -> {
            registerUserWithPreference();

            List<Story> availableStories = getArticles();

            //Here's where the magic starts
            AWSCredentials awsCredentials = new BasicAWSCredentials("XXX", "YYY");
            AWSCredentialsProvider awsCredentialsProvider = new AWSStaticCredentialsProvider(awsCredentials);

            AmazonComprehend amazonComprehendClient = AmazonComprehendClientBuilder.standard().withCredentials(awsCredentialsProvider).withRegion(Regions.DEFAULT_REGION).build();
            AmazonRekognition rekognitionClient = AmazonRekognitionClientBuilder.standard().withCredentials(awsCredentialsProvider).withRegion(Regions.DEFAULT_REGION).build();

            System.out.println("Retrieving articles...");
            availableStories.forEach(story -> {
                System.out.println("[FROM]" + story.getSource() + " --> " + story.getTitle());

                try {
                    Set<String> tags = new HashSet<>();
                    analyzeText(amazonComprehendClient, story, tags);
                    analyzeImage(rekognitionClient, story, tags);

                    storyRepository.save(story);
                } catch (Exception e) {
                    System.out.println("Oops! Something went wrong!");
                    e.printStackTrace();
                }
            });
            System.out.println("Done!");
        };
    }


    private void registerUserWithPreference() {
        Preference positivePreference = preferenceRepository.save(new Preference("positive"));
        Preference sportPreference = preferenceRepository.save(new Preference("sports"));
        Preference politicsPreference = preferenceRepository.save(new Preference("politics"));
        Preference happyPreference = preferenceRepository.save(new Preference("happy"));
        Preference technologyPreference = preferenceRepository.save(new Preference("technology"));
        Preference femalePreference = preferenceRepository.save(new Preference("female"));
        Preference animePreference = preferenceRepository.save(new Preference("anime"));
        Preference peoplePreference = preferenceRepository.save(new Preference("people"));
        Preference nightLifePreference = preferenceRepository.save(new Preference("night life"));

        User loggedInUser = new User("Jose");
        loggedInUser.setPreferences(Arrays.asList(positivePreference, sportPreference, politicsPreference, happyPreference, technologyPreference, femalePreference, animePreference, peoplePreference, nightLifePreference));
        userRepository.save(loggedInUser);
    }

    private List<Story> getArticles() {
        Story sports = new Story();
        sports.setSource("Facebook");
        sports.setTitle("Paul Pogba and France seek to erase demons and claim second World Cup ");
        sports.setContent("he tears have dried, according to Blaise Matuidi, but the memory continues to burn. France have powered their way to the World Cup final, where they will start as favourites against Croatia in Moscow, and it is to their credit that it will be a fifth major final in 20 years. Yet the journey to the point where a nation can dream was ignited by the nightmare of the last of them.");
        sports.setLink("https://www.theguardian.com/football/2018/jul/13/paul-pogba-france-erase-demons-second-world-cup");
        sports.setImage("https://i.guim.co.uk/img/media/d9d3212cc6e6ee2376b65d7400167bf2c6b24fa3/0_69_2529_1517/master/2529.jpg?w=1920&q=55&auto=format&usm=12&fit=max&s=9ddb6af2023f52856e78a3c0433fcafb");
        sports.setLocalImagePath("/Users/orvyl/Documents/AWS_HACK/pro-ductive-service/sports.jpg");

        Story politics = new Story();
        politics.setSource("Rappler");
        politics.setTitle("Duterte reappoints fraternity brod, this time to Comelec");
        politics.setContent("President Rodrigo Duterte has appointed one of his fraternity brothers at the San Beda University-based Lex Talionis Fraternitas as a commissioner of the Commission on Elections (Comelec). Former Justice Undersecretary Antonio Kho Jr. was named to the poll body to fill the seat left vacant by Comelec chair Sheriff Abas.");
        politics.setLink("http://newsinfo.inquirer.net/1010105/duterte-reappoints-fraternity-brod-this-time-to-comelec");
        politics.setImage("http://newsinfo.inquirer.net/files/2018/07/20180104MB-10-1.jpg");
        politics.setLocalImagePath("/Users/orvyl/Documents/AWS_HACK/pro-ductive-service/politics.jpg");

        Story happyFemaleSports = new Story();
        happyFemaleSports.setSource("ESPN");
        happyFemaleSports.setTitle("World Cup 2018: Croatia fans ecstatic after ousting England");
        happyFemaleSports.setContent("Right before the end of the game, a young man in a red and white Croatia football shirt rushed up to me at the fan zone in central Zagreb. \"If we win, I'm going to jump into that fountain,\" he said. Seconds later, as huge victory cheers filled the square, he plunged in, followed by dozens of others, shouting and splashing with joy. ");
        happyFemaleSports.setLink("https://www.bbc.com/news/world-europe-44801849");
        happyFemaleSports.setImage("https://ichef.bbci.co.uk/news/320/cpsprodpb/10CBE/production/_102489786_croatiafanshappy.jpg");
        happyFemaleSports.setLocalImagePath("/Users/orvyl/Documents/AWS_HACK/pro-ductive-service/happyFemale.jpg");

        Story anime = new Story();
        anime.setSource("GMA News");
        anime.setTitle("How to Identify the Basic Types of Anime and Manga");
        anime.setContent("When talking about anime and manga, there are a lot of Japanese terms thrown around that may be more than a little confusing to the uninitiated. But perhaps the most important of these to know refer to some of the different types of anime and manga. ");
        anime.setLink("https://kotaku.com/how-to-identify-the-basic-types-of-anime-and-manga-1538285518");
        anime.setImage("https://i.kinja-img.com/gawker-media/image/upload/s--EGZCXmQs--/c_scale,f_auto,fl_progressive,q_80,w_800/h00khfzy5r2r2un4pnpk.jpg");
        anime.setLocalImagePath("/Users/orvyl/Documents/AWS_HACK/pro-ductive-service/anime.jpg");

        Story politics1 = new Story();
        politics1.setSource("Vanity Fair");
        politics1.setTitle("Donald Trump Kept the Queen Waiting for Tea");
        politics1.setContent("Until Friday, Donald Trump’s history with the royal family was pretty much limited to his boasting to Howard Stern, shortly after her death in 1997, that he could have slept with Princess Diana. Then, in the midst of a European tour that has included shaking up NATO allies and insulting British Prime Minister Theresa May, Trump and his wife, Melania, did what two years ago would have been unthinkable: they had tea with the Queen.");
        politics1.setLink("https://www.vanityfair.com/style/2018/07/donald-trump-meets-the-queen-at-windsor-castle");
        politics1.setImage("https://media.vanityfair.com/photos/5b48d6cc4da5840c0f8c07e3/master/w_1920,c_limit/trump-meets-queen.jpg");
        politics1.setLocalImagePath("/Users/orvyl/Documents/AWS_HACK/pro-ductive-service/politics1.jpg");

        Story technology = new Story();
        technology.setSource("The Verge");
        technology.setTitle("PC sales are growing for the first time in six years");
        technology.setContent("Shipments of PCs are definitely growing, for the first time in six years. Market research firms Gartner and IDC both agree that the PC market grew in the second quarter of 2018, with IDC claiming an increase of 2.7 percent and Gartner recording a more modest 1.4 percent of growth. IDC first revealed the PC market was starting to flatten and show potential growth last year, but both Gartner and IDC track PC shipments differently.");
        technology.setLink("https://www.theverge.com/2018/7/13/17567760/pc-sales-growth-idc-gartner-july-2018");
        technology.setImage("https://cdn.vox-cdn.com/thumbor/cwQa3I4LjaXRtdkXD1uBi-I2ENU=/0x0:2040x1360/1820x1213/filters:focal(801x1018:1127x1344)/cdn.vox-cdn.com/uploads/chorus_image/image/60357813/akrales_180628_2695_0007.0.jpg");
        technology.setLocalImagePath("/Users/orvyl/Documents/AWS_HACK/pro-ductive-service/technology.jpg");

        Story technology1 = new Story();
        technology1.setSource("CNET");
        technology1.setTitle("Inside Facebook, Twitter and Google's AI battle over your social lives");
        technology1.setContent("When you sign up for Facebook on your phone, the app isn't just giving you the latest updates and photos from your friends and family. In the background, it's utilizing the phone's gyroscope to detect subtle movements that come from breathing. It's measuring how quickly you tap on the screen, and even looking at what angle the phone is being held.");
        technology1.setLink("https://www.cnet.com/news/inside-facebook-twitter-and-googles-ai-battle-over-your-social-lives/?utm_source=reddit.com");
        technology1.setImage("https://cnet3.cbsistatic.com/img/wIGZfTKZHYjhsS_wsY0lTzEme7k=/1600x900/2018/07/12/73042d77-b698-497a-94a6-d1576cd1f38e/gettyimages-905147816.jpg");
        technology1.setLocalImagePath("/Users/orvyl/Documents/AWS_HACK/pro-ductive-service/technology1.jpg");

        Story nightLife1 = new Story();
        nightLife1.setSource("CNET");
        nightLife1.setTitle("Philippines’ Best Bars and Lounges 2018");
        nightLife1.setContent("Find out where to go for the best city views, breezy outdoor escapes and chic spots that are sure to have you sipping and toasting all night long. Here are the country's top bars for 2018—from upbeat to laid back; large scale to intimate");
        nightLife1.setLink("https://ph.asiatatler.com/dining/philippines-best-bars-and-lounges-2018");
        nightLife1.setImage("https://cdn.asiatatler.com/asiatatler/ph/i/2018/04/story/41864/18204756-Curator-c18205429-1584x780.jpg");
        nightLife1.setLocalImagePath("/Users/orvyl/Documents/AWS_HACK/pro-ductive-service/nightLife1.jpg");

        Story happy = new Story();
        happy.setSource("Dissect Trends");
        happy.setTitle("Meet the Woman Who saved 30,000 Cats in 26 Years ");
        happy.setContent("Lynea Lattanzio, the incredible woman who founded and runs The Cat House on the Kings has saved 30,000 cats in 26 years. The sanctuary provides a safe and loving environment to abandoned, homeless and rescued cats. It is also the home of more than 700 cats and 300 kittens. ");
        happy.setLink("https://dissect-trends.blogspot.com/2018/07/meet-woman-who-saved-30000-cats-in-26.html");
        happy.setImage("https://1.bp.blogspot.com/--IdI_DJVpxI/W0XSuC2qreI/AAAAAAAAAEk/6XMgbdae6gIyuZVW2sD1U_4y2yrPbX_3ACLcBGAs/s640/cat%2Bhouse%2Bon%2Bthe%2Bkings.png");
        happy.setLocalImagePath("/Users/orvyl/Documents/AWS_HACK/pro-ductive-service/happy.png");

        Story happy1 = new Story();
        happy1.setSource("Medium");
        happy1.setTitle("$20M Raised to Help Reunite Families Separated at US Border");
        happy1.setContent("In a heart wrenching 8 minute audio heard around the world, the sound of children screaming and crying for their families shattered all barriers in the political world. Their sobs and begging were heard by everyone from President Donald Trump, all the way down to US citizens simply scrolling through their Facebook newsfeed.");
        happy1.setLink("https://medium.com/@inkind/20m-raised-to-help-reunite-families-separated-at-us-border-56a8e3b835ef");
        happy1.setImage("https://cdn-images-1.medium.com/max/1200/1*lgSVqKw6QZ7Sctn08UQUng.jpeg");
        happy1.setLocalImagePath("/Users/orvyl/Documents/AWS_HACK/pro-ductive-service/happy1.jpeg");

        Story anime1 = new Story();
        anime1.setSource("Anime News Network");
        anime1.setTitle("Dragon Ball Super Film Reveals Dragon Ball Super: Broly Title, Visual");
        anime1.setContent("Toei announced more details on Monday for the the upcoming 20th Dragon Ball film, including the film's title and visual. The film's title is Dragon Ball Super: Broly. The below visual's tagline reads \"The greatest enemy, Saiyan,\" and confirms the film will screen in IMAX, MX4D, and 4DX in Japan in addition to regular screenings. ");
        anime1.setLink("https://www.animenewsnetwork.com/news/2018-07-09/dragon-ball-super-film-reveals-dragon-ball-super-broly-title-visual/.133123");
        anime1.setImage("https://cdn.animenewsnetwork.com/thumbnails/max750x750/cms/news/133123/dbs-broly-film-visual.jpg");
        anime1.setLocalImagePath("/Users/orvyl/Documents/AWS_HACK/pro-ductive-service/anime1.jpg");

        Story sports1 = new Story();
        sports1.setSource("Yahoo");
        sports1.setTitle("Hall of Fame executive director: Terrell Owens won't be announced at ceremony");
        sports1.setContent("Owens has decided to not attend the Hall of Fame’s induction ceremony, even though he’s part of this year’s class. The HOF has said that’s unprecedented. Instead, Owens has planned to give his speech at the University of Tennessee at Chattanooga, where he played his college ball.");
        sports1.setLink("https://www.yahoo.com/amphtml/sports/hall-fame-executive-director-terrell-owens-wont-announced-ceremony-152409104.html");
        sports1.setImage("https://s.yimg.com/ny/api/res/1.2/mMTs2hFsg_janbOF0BeFFw--/YXBwaWQ9aGlnaGxhbmRlcjt3PTY0MDtoPTQwNS43Ng--/https://s.yimg.com/uu/api/res/1.2/0l41MyPYLQxkAeS7NrcGWw--~B/aD0xOTAyO3c9MzAwMDtzbT0xO2FwcGlkPXl0YWNoeW9u/http://media.zenfs.com/en/homerun/feed_manager_auto_publish_494/e6f4728318fc4472fd263ab3207d39de");
        sports1.setLocalImagePath("/Users/orvyl/Documents/AWS_HACK/pro-ductive-service/sports1.jpg");

        Story sports2 = new Story();
        sports2.setSource("Yahoo");
        sports2.setTitle("Tony Romo: Cowboys might have a No. 1 receiver on their roster");
        sports2.setContent("In Tony Romo’s 10 seasons as the Cowboys’ starting quarterback, he had five different players top 1,000 receiving yards a total of 11 times. Dez Bryant had three 1,000-yard seasons, Miles Austin two, Jason Witten two, Terrell Owens three and Terry Glenn one in Romo’s stint as the team’s starter.");
        sports2.setLink("https://sports.yahoo.com/tony-romo-cowboys-might-no-012511426.html");
        sports2.setImage("https://s3media.247sports.com/Uploads/Assets/859/497/6_8497859.jpg");
        sports2.setLocalImagePath("/Users/orvyl/Documents/AWS_HACK/pro-ductive-service/sports2.jpg");

        List<Story> availableStories = Arrays.asList(sports, politics, happyFemaleSports, anime, politics1, technology, technology1, nightLife1, happy, happy1, anime1, sports1, sports2);
        storyRepository.saveAll(availableStories);
        return availableStories;
    }

    private void analyzeText(AmazonComprehend amazonComprehendClient, Story story, Set<String> tags) {
        String text = story.getTitle() + ". " + story.getContent();
        DetectEntitiesRequest entitiesRequest = new DetectEntitiesRequest().withLanguageCode("en").withText(text);
        DetectEntitiesResult entitiesResult = amazonComprehendClient.detectEntities(entitiesRequest);

        DetectKeyPhrasesRequest keyPhrasesRequest = new DetectKeyPhrasesRequest().withText(text).withLanguageCode("en");
        DetectKeyPhrasesResult keyPhrasesResult = amazonComprehendClient.detectKeyPhrases(keyPhrasesRequest);

        DetectSentimentRequest sentimentRequest = new DetectSentimentRequest().withLanguageCode("en").withText(text);
        DetectSentimentResult sentimentResult = amazonComprehendClient.detectSentiment(sentimentRequest);

        entitiesResult.getEntities().stream()
                .filter(entity -> entity.getScore() > .9 && entity.getText().split(" ").length <= 3)
                .forEach(entity -> {
                    String e = entity.getText().toLowerCase();
                    if (tags.add(e)) {
                        story.getTags().add(e);
                    }
                });
        keyPhrasesResult.getKeyPhrases().stream()
                .filter(keyPhrase -> keyPhrase.getScore() > .9 && keyPhrase.getText().split(" ").length <= 3)
                .forEach(keyPhrase -> {
                    String e = keyPhrase.getText().toLowerCase();
                    if (tags.add(e)) {
                        story.getTags().add(e);
                    }
                });

        story.getTags().add(sentimentResult.getSentiment().toLowerCase());
    }

    private void analyzeImage(AmazonRekognition rekognitionClient, Story story, Set<String> tags) throws IOException {
        ByteBuffer imageBytes;
        try (InputStream inputStream = new FileInputStream(new File(story.getLocalImagePath()))) {
            imageBytes = ByteBuffer.wrap(IOUtils.toByteArray(inputStream));
        }

        DetectLabelsRequest request = new DetectLabelsRequest()
                .withImage(new Image()
                        .withBytes(imageBytes))
                .withMaxLabels(10)
                .withMinConfidence(77F);

        DetectLabelsResult labelsResult = rekognitionClient.detectLabels(request);
        labelsResult.getLabels().forEach(label -> {
            String e = label.getName();
            if (tags.add(e)) {
                story.getTags().add(e.toLowerCase());
            }
            System.out.println(story.getLocalImagePath() + " " + e + " " + label.getConfidence());
        });
    }
}
