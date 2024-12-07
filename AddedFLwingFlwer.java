package TestCode;

import java.io.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

import javax.swing.JFrame;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.CategoryLabelPositions;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.category.DefaultCategoryDataset;

import TestCode.test5.Tweet;

public class test4 {

    private static final double DAMPING_FACTOR = 0.85;
    private static final int MAX_ITER = 100;
    private static final double TOL = 1e-6;

    /*public static Map<String, Double> computePageRank(Map<String, Set<String>> graph) {    	
        int numNodes = graph.size();
        Map<String, Double> rank = new ConcurrentHashMap<>();
        Map<String, Double> newRank = new ConcurrentHashMap<>();
        AtomicBoolean converged = new AtomicBoolean(false);

        // Khoi tao gia tri moi nut
        for (String node : graph.keySet()) {
            rank.put(node, 1.0 / numNodes);
        }

        // Iterative computation
        for (int iter = 0; iter < MAX_ITER; iter++) {
            converged.set(true);

            // Parallel computation of new PageRank values
            ForkJoinPool.commonPool().submit(() -> graph.keySet().parallelStream().forEach(node -> {
                double rankSum = 0.0;

                // Cong thuc tinh pagerank
                for (String neighbor : graph.keySet()) {
                    if (graph.get(neighbor).contains(node)) {
                        rankSum += rank.get(neighbor) / graph.get(neighbor).size();
                    }
                }

                double updatedRank = (1 - DAMPING_FACTOR) / numNodes + DAMPING_FACTOR * rankSum;
                newRank.put(node, updatedRank);

                // Kiem tra lap
                if (Math.abs(updatedRank - rank.get(node)) > TOL) {
                    converged.set(false);
                }
            })).join();

            // Cap nhat gia tri rank
            rank.putAll(newRank);

            // Thoat vong lap
            if (converged.get()) {
                break;
            }
        }

        return rank;
    }*/
    
    public static Map<String, Double> computePageRank(
    	    Map<String, Map<String, Double>> graph,
    	    Map<String, Integer> followers,
    	    Map<String, Integer> followings
    	) {
    	    int numNodes = graph.size();
    	    Map<String, Double> rank = new ConcurrentHashMap<>();
    	    Map<String, Double> newRank = new ConcurrentHashMap<>();
    	    AtomicBoolean converged = new AtomicBoolean(false);

    	    // Khởi tạo giá trị PageRank cho mỗi nút
    	    for (String node : graph.keySet()) {
    	        rank.put(node, 1.0 / numNodes);
    	    }

    	    for (int iter = 0; iter < MAX_ITER; iter++) {
    	        converged.set(true);

    	        ForkJoinPool.commonPool().submit(() -> graph.keySet().parallelStream().forEach(node -> {
    	            double rankSum = 0.0;

    	            // Tính tổng PageRank từ các nút khác có liên kết đến nút hiện tại
    	            for (String neighbor : graph.keySet()) {
    	                if (graph.get(neighbor).containsKey(node)) {
    	                    double weight = graph.get(neighbor).get(node);
    	                    double totalWeight = graph.get(neighbor).values().stream().mapToDouble(Double::doubleValue).sum();
    	                    int followerCount = followers.getOrDefault(neighbor, 1);
    	                    int followingCount = followings.getOrDefault(neighbor, 1);

    	                    // Điều chỉnh trọng số dựa trên follower và following
    	                    rankSum += (weight / totalWeight) * rank.get(neighbor) * (followerCount / (double) followingCount);
    	                }
    	            }

    	            // Cập nhật PageRank với công thức trọng số
    	            double updatedRank = (1 - DAMPING_FACTOR) / numNodes + DAMPING_FACTOR * rankSum;
    	            newRank.put(node, updatedRank);

    	            if (Math.abs(updatedRank - rank.get(node)) > TOL) {
    	                converged.set(false);
    	            }
    	        })).join();

    	        rank.putAll(newRank);

    	        if (converged.get()) {
    	            break;
    	        }
    	    }

    	    return rank;
    	}

    private static int parseInteger(String value, String fieldName) {
        try {
            return Integer.parseInt(value.trim());
        } catch (NumberFormatException e) {
            return 0; // Trả về giá trị mặc định là 0 nếu có lỗi
        }
    }
       
    public static void main(String[] args) throws FileNotFoundException, IOException {
        // Example: Large-scale graph representation
    	Map<String, Map<String, Double>> graph = new HashMap<>();
    	Map<String, Integer> followers = new HashMap<>();
    	Map<String, Integer> followings = new HashMap<>();
    	
        String filePath = "/Users/kinghieu14/Downloads/kol_information.txt";
        
        // Đọc dữ liệu từ file và xây dựng đồ thị
        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String line;
            
            while ((line = br.readLine()) != null) {
            	line = line.trim();
            	
                if(line.isEmpty()) continue;
                
                String kolId = line;
  //              System.out.println(kolId);
                String kolName = br.readLine().trim();
   //             System.out.println(kolName);
                int following = parseInteger(br.readLine().trim(), "following");
  //              System.out.println(following);
                int follower = parseInteger(br.readLine().trim(), "follower");
  //              System.out.println(follower);
                
                followings.put(kolId, following);
                followers.put(kolId, follower);
                
                String tweetData = br.readLine().trim();
                String[] tweets = tweetData.split("/");
                for (String tweet : tweets) {
                	String[] tweetParts = tweet.split(",");
                	if (tweetParts.length == 4) {
                		try {
                			String targetId = tweetParts[0];
                			int likes = Integer.parseInt(tweetParts[1]);
                			int comments = Integer.parseInt(tweetParts[2]);
                			int shares = Integer.parseInt(tweetParts[3]);
//                			System.out.println(targetId);
 //               			System.out.println(likes);
//                			System.out.println(comments);
 //               			System.out.println(shares);
                			
                			double weight = likes * 0.5 + comments * 0.3 + shares * 0.2;
  //             			System.out.println(weight);
                			
                            graph.putIfAbsent(kolName, new HashMap<>());
                            graph.putIfAbsent(targetId, new HashMap<>());

                            graph.get(kolName).put(targetId, weight);
                            graph.get(targetId).put(kolName, weight);
                		} catch (NumberFormatException e) {
                		}
                	}
                }
                
            }
            
/*            String kolId = br.readLine().trim(); // ID KOL
            String kolName = br.readLine().trim(); // Tên KOL
            System.out.println(kolId);
            System.out.println(kolName + "\n");
            int numFollowing = Integer.parseInt(br.readLine().trim()); // Số lượng following
            int numFollowers = Integer.parseInt(br.readLine().trim()); // Số lượng follower

            followings.put(kolName, numFollowing);
            followers.put(kolName, numFollowers);

            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty()) continue;

                String[] interactions = line.split("/");
                for (String interaction : interactions) {
                    String[] parts = interaction.split(",");
                    if (parts.length < 4) continue;

                    String targetId = parts[0].trim();
                    double likes = Double.parseDouble(parts[1].trim());
                    double comments = Double.parseDouble(parts[2].trim());
                    double shares = Double.parseDouble(parts[3].trim());

                    double weight = likes * 0.5 + comments * 0.3 + shares * 0.2;

                    graph.putIfAbsent(kolName, new HashMap<>());
                    graph.putIfAbsent(targetId, new HashMap<>());

                    graph.get(kolName).put(targetId, weight);
                    graph.get(targetId).put(kolName, weight);
                }
            }*/
        }

        // Tinh pagerank
        //long startTime = System.currentTimeMillis();
        Map<String, Double> pageRanks = computePageRank(graph, followers, followings);
        //long endTime = System.currentTimeMillis();
        
     // Lấy Top KOL (thứ tự giảm dần của điểm PageRank)
        List<Map.Entry<String, Double>> sortedKOLs = pageRanks.entrySet().stream()
                .sorted((a, b) -> Double.compare(b.getValue(), a.getValue()))
                .limit(10) // Lấy Top 5
                .collect(Collectors.toList());

        // Tạo dataset cho biểu đồ
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        for (Map.Entry<String, Double> entry : sortedKOLs) {
            dataset.addValue(entry.getValue(), "PageRank", entry.getKey());
        }

        // Tạo biểu đồ cột
        JFreeChart barChart = ChartFactory.createBarChart(
                "Top KOLs by PageRank",
                "KOLs",
                "PageRank Score",
                dataset,
                PlotOrientation.VERTICAL,
                true, true, false);

        // Tùy chỉnh trục
        CategoryAxis domainAxis = barChart.getCategoryPlot().getDomainAxis();
        domainAxis.setCategoryLabelPositions(CategoryLabelPositions.UP_45);

        // Hiển thị biểu đồ
        JFrame frame = new JFrame();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(1000, 800);
        frame.add(new ChartPanel(barChart));
        frame.setVisible(true);
        
        // In ket qua
        System.out.println("PageRank Scores:");
        for (Map.Entry<String, Double> entry : pageRanks.entrySet()) {
            System.out.println(entry.getKey() + ": " + entry.getValue());
        }
        
        //System.out.println("Computation Time: " + (endTime - startTime) + "ms");
     }
}
