/*
 * This file is part of a project by Tassu_.
 * Usage of this file (or parts of it) is not allowed
 * without a permission from Tassu_.
 *
 * You may contact Tassu_ by e-mailing to <tassu@tassu.me>.
 *
 * Current Package: me.tassu.queue.file
 *
 * @author tassu
 */

package me.tassu.queue.file;

import net.md_5.bungee.config.Configuration;
import net.md_5.bungee.config.ConfigurationProvider;
import net.md_5.bungee.config.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.List;

/**
 * This class represents a configuration file.
 * @author tassu
 * @since 1.0
 * @todo Documentation
 */
@SuppressWarnings("unused") // tassu
public class ConfigFile {

    private File file;

    private Configuration config;

    ConfigFile(File file) throws IOException {
        this.file = file;
        reloadConfig();
    }

    /**
     * Reloads the configuration file from disk.
     * @throws IOException thrown when reading the file from disk fails
     */
    public void reloadConfig() throws IOException {
        this.config = ConfigurationProvider.getProvider(YamlConfiguration.class).load(file);
    }

    ///////////////////////////////////////////////////
    //             START IMPLEMENTATION
    ///////////////////////////////////////////////////

    /**
     * Returns value from config.
     * @param path configuration path for the value
     * @param def value to be returned if it was not defined on configuration
     * @param <T> type of the value
     * @return value from config, or {@param def} if not defined on config
     */
    public <T> T get(String path, T def) {
        return config.get(path, def);
    }

    /**
     * Returns value from config.
     * @param path configuration path for the value
     * @return value from config
     */
    public Object get(String path) {
        return config.get(path);
    }

    /**
     * gets the default value for specified path
     * @param path config path
     * @return default value
     */
    public Object getDefault(String path) {
        return config.getDefault(path);
    }

    /**
     * sets a value to the configuration
     * note: does not save the config
     * @param path path on configuration
     * @param value the value
     */
    public void set(String path, Object value) {
        config.set(path, value);
    }

    /**
     * gets a section as a {@link Configuration} object
     * @param path config path
     * @return the section
     */
    public Configuration getSection(String path) {
        return config.getSection(path);
    }

    /**
     * gets all keys of this configuration
     * @return all keys
     */
    public Collection<String> getKeys() {
        return config.getKeys();
    }

    /**
     * gets a byte object from config
     * @param path the path
     * @return the byte
     */
    public byte getByte(String path) {
        return config.getByte(path);
    }

    /**
     * gets a byte object from config
     * @param path the path
     * @param def value to be used when not defined in config
     * @return the byte, or <code>def</code> when not specified
     */
    public byte getByte(String path, byte def) {
        return config.getByte(path, def);
    }

    public List<Byte> getByteList(String path) {
        return config.getByteList(path);
    }

    public short getShort(String path) {
        return config.getShort(path);
    }

    public short getShort(String path, short def) {
        return config.getShort(path, def);
    }

    public List<Short> getShortList(String path) {
        return config.getShortList(path);
    }

    public int getInt(String path) {
        return config.getInt(path);
    }

    public int getInt(String path, int def) {
        return config.getInt(path, def);
    }

    public List<Integer> getIntList(String path) {
        return config.getIntList(path);
    }

    public long getLong(String path) {
        return config.getLong(path);
    }

    public long getLong(String path, long def) {
        return config.getLong(path, def);
    }

    public List<Long> getLongList(String path) {
        return config.getLongList(path);
    }

    public float getFloat(String path) {
        return config.getFloat(path);
    }

    public float getFloat(String path, float def) {
        return config.getFloat(path, def);
    }

    public List<Float> getFloatList(String path) {
        return config.getFloatList(path);
    }

    public double getDouble(String path) {
        return config.getDouble(path);
    }

    public double getDouble(String path, double def) {
        return config.getDouble(path, def);
    }

    public List<Double> getDoubleList(String path) {
        return config.getDoubleList(path);
    }

    public boolean getBoolean(String path) {
        return config.getBoolean(path);
    }

    public boolean getBoolean(String path, boolean def) {
        return config.getBoolean(path, def);
    }

    public List<Boolean> getBooleanList(String path) {
        return config.getBooleanList(path);
    }

    public char getChar(String path) {
        return config.getChar(path);
    }

    public char getChar(String path, char def) {
        return config.getChar(path, def);
    }

    public List<Character> getCharList(String path) {
        return config.getCharList(path);
    }

    public String getString(String path) {
        return config.getString(path);
    }

    public String getString(String path, String def) {
        return config.getString(path, def);
    }

    public List<String> getStringList(String path) {
        return config.getStringList(path);
    }

    public List<?> getList(String path) {
        return config.getList(path);
    }

    public List<?> getList(String path, List<?> def) {
        return config.getList(path, def);
    }
}
