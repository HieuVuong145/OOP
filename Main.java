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

public class Main {

    private static final double DAMPING_FACTOR = 0.85;
    private static final int MAX_ITER = 100;
    private static final double TOL = 1e-6;

    public static Map<String, Double> computePageRank(Map<String, Set<String>> graph) {    	
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
    }

    
    public static void main(String[] args) throws FileNotFoundException, IOException {
        // Example: Large-scale graph representation
        Map<String, Set<String>> graph = new HashMap<>();

        String filePath = "/Users/kinghieu14/Downloads/kol_list_check.txt";
        // Đọc dữ liệu từ file và xây dựng đồ thị
        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String line;

            while ((line = br.readLine()) != null) {
                line = line.trim();

                // Bỏ qua dòng trống
                if (line.isEmpty()) continue;

                // Đọc thông tin user
                String username = br.readLine().trim(); // Đọc username
                br.readLine(); // Bỏ qua số following
                br.readLine(); // Bỏ qua số followers
                String tweetData = br.readLine().trim();

                // Tạo nút user
                graph.putIfAbsent(username, new HashSet<>());

                // Kết nối user với các tweet
                String[] tweets = tweetData.split("/");
                for (String tweet : tweets) {
                    String tweetId = tweet.split(",")[0]; // ID tweet
                    graph.putIfAbsent(tweetId, new HashSet<>()); // Tạo nút tweet nếu chưa có
                    graph.get(username).add(tweetId); // user -> tweet
                    graph.get(tweetId).add(username); // tweet -> user
                }
            }

        // Tinh pagerank
        //long startTime = System.currentTimeMillis();
        Map<String, Double> pageRanks = computePageRank(graph);
        //long endTime = System.currentTimeMillis();
        
     // Lấy Top KOL (thứ tự giảm dần của điểm PageRank)
        List<Map.Entry<String, Double>> sortedKOLs = pageRanks.entrySet().stream()
                .sorted((a, b) -> Double.compare(b.getValue(), a.getValue()))
                .limit(5) // Lấy Top 5
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
}