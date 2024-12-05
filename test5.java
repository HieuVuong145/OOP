import java.io.*;
import java.util.*;

public class test5 {

    public static void main(String[] args) {
        String filePath = "/Users/kinghieu14/Downloads/kol_list_check.txt";

        // HashMap cho user và tweet
        HashMap<String, User> userMap = new HashMap<>();

        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String line;

            while ((line = br.readLine()) != null) {
                line = line.trim(); // Xóa khoảng trắng đầu/cuối dòng

                // Bỏ qua các dòng trống
                if (line.isEmpty()) continue;

                // Đọc data
                String userId = line;
                String username = br.readLine().trim();
                int following = parseInteger(br.readLine().trim(), "following");
                int followers = parseInteger(br.readLine().trim(), "followers");
                String tweetData = br.readLine().trim();

                // Phân tích dữ liệu tweet
                List<Tweet> tweets = parseTweets(tweetData);

                // Tạo User và lưu vào 
                User user = new User(userId, username, following, followers, tweets);
                userMap.put(userId, user);
            }

            // In ra kết quả
            for (Map.Entry<String, User> entry : userMap.entrySet()) {
                System.out.println("User ID: " + entry.getKey());
                System.out.println(entry.getValue());
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Phân tích data tweet thành đối tượng 
    private static List<Tweet> parseTweets(String tweetData) {
        List<Tweet> tweets = new ArrayList<>();
        String[] tweetEntries = tweetData.split("/"); // Các tweet phân  bằng dấu "/"

        for (String entry : tweetEntries) {
            String[] tweetParts = entry.split(","); // ID tweet,likes,comments,reposts
            if (tweetParts.length == 4) {
                try {
                    String tweetId = tweetParts[0];
                    int likes = Integer.parseInt(tweetParts[1]);
                    int comments = Integer.parseInt(tweetParts[2]);
                    int reposts = Integer.parseInt(tweetParts[3]);
                    tweets.add(new Tweet(tweetId, likes, comments, reposts));
                } catch (NumberFormatException e) {
                    System.err.println("Lỗi định dạng dữ liệu tweet: " + entry);
                }
            }
        }
        return tweets;
    }

    // doi string sang int
    private static int parseInteger(String value, String fieldName) {
        try {
            return Integer.parseInt(value.trim());
        } catch (NumberFormatException e) {
            System.err.println("Lỗi định dạng số ở trường " + fieldName + ": " + value);
            return 0; // Trả về giá trị mặc định là 0 nếu có lỗi
        }
    }

    static class User {
        private String userId;
        private String username;
        private int following;
        private int followers;
        private List<Tweet> tweets;

        public User(String userId, String username, int following, int followers, List<Tweet> tweets) {
            this.userId = userId;
            this.username = username;
            this.following = following;
            this.followers = followers;
            this.tweets = tweets;
        }

        public String getUserId() {
            return userId;
        }

        public void setUserId(String userId) {
            this.userId = userId;
        }

        public String getUsername() {
            return username;
        }

        public void setUsername(String username) {
            this.username = username;
        }

        public int getFollowing() {
            return following;
        }

        public void setFollowing(int following) {
            this.following = following;
        }

        public int getFollowers() {
            return followers;
        }

        public void setFollowers(int followers) {
            this.followers = followers;
        }

        public List<Tweet> getTweets() {
            return tweets;
        }

        public void setTweets(List<Tweet> tweets) {
            this.tweets = tweets;
        }

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            sb.append("Username: ").append(username)
              .append("\nFollowing: ").append(following)
              .append("\nFollowers: ").append(followers)
              .append("\nTweets:");
            for (Tweet tweet : tweets) {
                sb.append("\n  ").append(tweet);
            }
            return sb.toString();
        }
    }

        static class Tweet {
        private String tweetId;
        private int likes;
        private int comments;
        private int reposts;

        public Tweet(String tweetId, int likes, int comments, int reposts) {
            this.tweetId = tweetId;
            this.likes = likes;
            this.comments = comments;
            this.reposts = reposts;
        }

        public String getTweetId() {
            return tweetId;
        }

        public void setTweetId(String tweetId) {
            this.tweetId = tweetId;
        }

        public int getLikes() {
            return likes;
        }

        public void setLikes(int likes) {
            this.likes = likes;
        }

        public int getComments() {
            return comments;
        }

        public void setComments(int comments) {
            this.comments = comments;
        }

        public int getReposts() {
            return reposts;
        }

        public void setReposts(int reposts) {
            this.reposts = reposts;
        }

        @Override
        public String toString() {
            return "Tweet ID: " + tweetId + ", Likes: " + likes +
                   ", Comments: " + comments + ", Reposts: " + reposts;
        }
    }
}
