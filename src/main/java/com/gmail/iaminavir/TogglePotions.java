package com.gmail.iaminavir;


import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffectType;

import java.io.*;
import java.nio.file.Files;
import java.util.HashMap;

public class TogglePotions extends JavaPlugin {

    private HashMap<String, PotionEffectType> potionAssociations;

    public HashMap<String, PotionEffectType> getPotionAssociations() {
        return potionAssociations;
    }

    @Override
    public void onEnable(){

        this.saveDefaultConfig();
        potionAssociations = registerAssociations();

        this.getCommand("toggle").setExecutor(new ToggleCommand(this));
    }

    public void onDisable(){

    }

    @Override
    public void reloadConfig() {
        super.reloadConfig();
        potionAssociations = registerAssociations();
    }

    private void copyAssociationsFromJar(){
        File file = new File(getDataFolder(),"potionMap.txt");
        if (!file.exists()) {
            InputStream link = getResource("potionMap.txt");
            try {
                Files.copy(link, file.getAbsoluteFile().toPath());
        }
            catch (IOException e){
                System.out.print("TogglePotions: Unable to make potion mapping file! Plugin will not enable.");
                this.setEnabled(false);
            }
        }
    }

    public HashMap<String, PotionEffectType> registerAssociations(){
        copyAssociationsFromJar();
        HashMap<String, PotionEffectType> associations = new HashMap<String, PotionEffectType>();
        try{
            BufferedReader br = new BufferedReader(new FileReader(new File(getDataFolder(), "potionMap.txt")));

            String line;
            while((line = br.readLine()) != null){
                String[] values = line.split("\\|");
                associations.put(values[0], PotionEffectType.getByName(values[1]));
            }
            br.close();
        }
        catch (FileNotFoundException e){
            System.out.print("TogglePotions: Association file is missing!");
        }
        catch (IOException e){
            System.out.print("TogglePotions: Unable to access association file!");
        }
        return associations;
    }
}
