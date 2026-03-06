package client.controller;

import client.net.ClientSocket;
import common.model.Journal;
import common.net.Message;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class JournalController {
    private ClientSocket clientSocket = new ClientSocket();

    // 缓存 arXiv 结果
    private List<Journal> cachedArxiv = null;
    private long lastFetchTime = 0;
    private static final long CACHE_VALID_MS = 10 * 60 * 1000; // 10分钟有效

    // 查询所有期刊
    public List<Journal> getAllJournals() {
        try {
            Message request = new Message();
            request.setType("getAllJournals");

            Message response = clientSocket.sendRequest(request);
            if ("success".equals(response.getStatus())) {
                return (List<Journal>) response.getData();
            } else {
                System.out.println("查询所有期刊失败：" + response.getMsg());
                return new ArrayList<>();
            }
        } catch (Exception e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    // 根据ID查询期刊
    public Journal getJournalById(int journalId) {
        try {
            Message request = new Message();
            request.setType("getJournalById");
            request.setData(journalId);

            Message response = clientSocket.sendRequest(request);
            if ("success".equals(response.getStatus())) {
                return (Journal) response.getData();
            } else {
                System.out.println("按ID查询期刊失败：" + response.getMsg());
                return null;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    // 新增期刊
    public boolean addJournal(Journal journal) {
        try {
            Message request = new Message();
            request.setType("addJournal");
            request.setData(journal);

            Message response = clientSocket.sendRequest(request);
            return "success".equals(response.getStatus());
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    // 更新期刊
    public boolean updateJournal(Journal journal) {
        try {
            Message request = new Message();
            request.setType("updateJournal");
            request.setData(journal);

            Message response = clientSocket.sendRequest(request);
            return "success".equals(response.getStatus());
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    // 删除期刊
    public boolean deleteJournal(int journalId) {
        try {
            Message request = new Message();
            request.setType("deleteJournal");
            request.setData(journalId);

            Message response = clientSocket.sendRequest(request);
            return "success".equals(response.getStatus());
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 搜索 arXiv 数据（支持缓存）
     */
    public List<Journal> searchJournals(String keyword) {
        if (keyword == null || keyword.isEmpty()) keyword = "all";

        // 仅对 "all" 做缓存
        if ("all".equals(keyword)) {
            if (cachedArxiv != null && System.currentTimeMillis() - lastFetchTime < CACHE_VALID_MS) {
                return cachedArxiv;
            }
        }

        List<Journal> list = fetchFromArxiv(keyword);

        if ("all".equals(keyword)) {
            cachedArxiv = list;
            lastFetchTime = System.currentTimeMillis();
        }
        return list;
    }

    /**
     * 实际调用 arXiv API
     */
    private List<Journal> fetchFromArxiv(String keyword) {
        List<Journal> list = new ArrayList<>();
        try {
            String query = URLEncoder.encode(keyword, "UTF-8");
            // 默认只取 10 条，加快速度
            String api = "https://export.arxiv.org/api/query?search_query=all:" + query + "&max_results=10";
            URL url = new URL(api);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");

            BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
            reader.close();

            String xml = sb.toString();
            String[] entries = xml.split("<entry>");
            int idCounter = 1;
            for (String entry : entries) {
                if (!entry.contains("</entry>")) continue;

                String title = extractTag(entry, "title");
                String publishedStr = extractTag(entry, "published");
                Date published = null;
                try {
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
                    sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
                    published = sdf.parse(publishedStr);
                } catch (Exception ignored) {}

                String summary = extractTag(entry, "summary");
                String link = extractTag(entry, "id");

                // 提取类别
                String category = "Unknown";
                Pattern p = Pattern.compile("<arxiv:primary_category[^>]*term=\"([^\"]+)\"");
                Matcher m = p.matcher(entry);
                if (m.find()) category = m.group(1);

                Journal j = new Journal();
                j.setJournalId(idCounter++);
                j.setName(title.replaceAll("\\s+", " ").trim());
                j.setCategory(category);
                j.setPublishDate(published);
                j.setPublisher("arXiv");
                j.setDescription(summary.replaceAll("\\s+", " ").trim());
                j.setLink(link);

                list.add(j);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }


    private String extractTag(String xml, String tag) {
        int start = xml.indexOf("<" + tag + ">");
        int end = xml.indexOf("</" + tag + ">");
        if (start >= 0 && end > start) {
            return xml.substring(start + tag.length() + 2, end).trim();
        }
        return "";
    }

    /**
     * 合并本地和 arXiv
     */
    public List<Journal> getAllJournalsCombined(String keyword) {
        List<Journal> list = new ArrayList<>();

        // 1. 本地
        list.addAll(getAllJournals());

        // 2. arXiv
        int arxivStartId = 100;
        for (Journal j : searchJournals(keyword)) {
            j.setJournalId(arxivStartId++);
            list.add(j);
        }

        return list;
    }

}
