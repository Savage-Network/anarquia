package com.minecraft.core.api.geography;

import com.minecraft.core.Constant;
import com.minecraft.core.Core;
import com.minecraft.core.util.list.http.HttpRequest;
import com.google.gson.JsonObject;

import java.util.logging.Level;

public class GeographyService {

    public static GeographyBuilder getGeography(String address) {
        try {
            HttpRequest request = HttpRequest.get("http://ip-api.com/json/" + address)
                    .connectTimeout(5000)
                    .readTimeout(5000)
                    .userAgent(Constant.SERVER_NAME.toLowerCase() + "/1.0.0")
                    .accept(HttpRequest.CONTENT_TYPE_JSON);

            int code = request.code();

            if (code == 200 || request.ok()) {
                JsonObject data = Core.PARSER.parse(request.reader()).getAsJsonObject();

                String status = data.get("status").getAsString();

                if (!status.equalsIgnoreCase("success")) return GeographyBuilder.builder().build();

                return GeographyBuilder.builder()
                        .address(address)
                        .country(data.get("country").getAsString())
                        .city(data.get("city").getAsString())
                        .region(data.get("regionName").getAsString())
                        .asn(data.get("as").getAsString())
                        .build();
            } else
                return GeographyBuilder.builder().build();

        } catch (Exception e) {
            Core.getLogger().log(Level.WARNING, "Não foi possível requisitar os dados do IP: " + address, e);
        }

        return GeographyBuilder.builder().build();
    }
}
