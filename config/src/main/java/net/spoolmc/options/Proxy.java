package net.spoolmc.options;

import com.google.gson.annotations.SerializedName;
import lombok.Getter;

public class Proxy {
    @Getter
    private ProxyType type;

    @Getter
    @SerializedName("velocity-secret")
    private String velocitySecret;
}
