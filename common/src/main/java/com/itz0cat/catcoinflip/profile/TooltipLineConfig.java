package com.itz0cat.catcoinflip.profile;

public class TooltipLineConfig {

    private String id;
    private String label;
    private String color;
    private String icon;
    private boolean enabled;

    public TooltipLineConfig() {}

    public TooltipLineConfig(String id, String label, String color, String icon, boolean enabled) {
        this.id = id;
        this.label = label;
        this.color = color;
        this.icon = icon;
        this.enabled = enabled;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getLabel() { return label; }
    public void setLabel(String label) { this.label = label; }

    public String getColor() { return color; }
    public void setColor(String color) { this.color = color; }

    public String getIcon() { return icon; }
    public void setIcon(String icon) { this.icon = icon; }

    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean enabled) { this.enabled = enabled; }
}
