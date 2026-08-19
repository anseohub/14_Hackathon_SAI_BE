package com.sai.sai_api.Dto;

import java.util.List;

public record AnalyzeResponse(String label, String sender, String receiver,String koPrefix,String enPrefix,List<Risk> risks) {
    public record Risk(String ko,String en,String title,String reason,Fix fix){}
    public record Fix(String ko, String en){}
}
