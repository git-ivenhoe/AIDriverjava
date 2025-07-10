package edu.cs4730.wearappvoice.ai.poi;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class PoiSearchResult {
    private String command = "";
    private String query = "";
    private String status = "";
    private int count = 0;
    private List<PoiInfo> poiList = new ArrayList<>();
    private String poisString;

    public PoiSearchResult(String jsonStr) {
        try {
            JSONObject root = new JSONObject(jsonStr);

            // 解析 cmd
            if (root.has("cmd")) {
                JSONObject cmd = root.getJSONObject("cmd");
                this.command = cmd.optString("cmd", "");

                if (cmd.has("args")) {
                    JSONObject args = cmd.getJSONObject("args");
                    this.query = args.optString("q", "");//获得查询什么内容，例如酒店、加油站
                }

                // 解析 result
                if (cmd.has("result")) {
                    JSONObject result = cmd.getJSONObject("result");

                    this.status = result.optString("status", "0");
                    this.count = parseSafeInt(result, "count", 0);

                    if (result.has("pois")) {
                        JSONArray pois = result.optJSONArray("pois");
                        poisString = pois.toString();
                        if (pois != null) {
                            for (int i = 0; i < pois.length(); i++) {
                                try {
                                    JSONObject poi = pois.getJSONObject(i);
                                    PoiInfo info = new PoiInfo(
                                            poi.optString("id", ""),
                                            poi.optString("name", ""),
                                            poi.optString("type", ""),
                                            poi.optString("address", ""),
                                            poi.optString("location", "")
                                    );
                                    poiList.add(info);
                                } catch (JSONException e) {
                                    System.err.println("POI 解析出错: " + e.getMessage());
                                }
                            }
                        }
                    }
                }
            }
        } catch (JSONException e) {
            System.err.println("JSON 格式解析失败: " + e.getMessage());
        }
    }

    private int parseSafeInt(JSONObject obj, String key, int defaultValue) {
        try {
            return obj.has(key) ? obj.getInt(key) : defaultValue;
        } catch (JSONException e) {
            return defaultValue;
        }
    }

    public String printSummary() {
        System.out.println("命令类型: " + command);
        System.out.println("查询内容: " + query);
        System.out.println("状态: " + status + ", 结果数量: " + count);
        for (PoiInfo poi : poiList) {
            System.out.printf("POI: %s | %s | %s | %s | 经度: %.6f, 纬度: %.6f\n",
                    poi.getId(), poi.getName(), poi.getType(), poi.getAddress(),
                    poi.getLongitude(), poi.getLatitude());
        }

        return "查询到周边" + count + "个" + query;
    }

    public String getQuery() {
        return query;
    }

    public String getPoisString() {
        return poisString;
    }
}

