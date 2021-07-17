package thesallab.configuration;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.DoubleNode;
import com.fasterxml.jackson.databind.node.IntNode;
import org.apache.commons.configuration.CompositeConfiguration;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.stream.StreamSupport;

/**
 * 配置信息。
 *
 * @author Zhang, Yin
 */
public class Config {

    // **************** 公开变量

    /**
     * 配置文件路径。
     */
    public static final String CONFIG_FILE = "cx.config.file";

    /**
     * 配置工作模式。
     */
    public static final String CONFIG_WORKING_MODE = "cx.config.workingmode";

    /**
     * 全局配置工作模式。
     */
    public static final String CONFIG_WORKING_MODE_GLOBAL = "global";

    /**
     * 线程本地工作模式。
     */
    public static final String CONFIG_WORKING_MODE_THREADLOCAL = "threadlocal";

    /**
     * 全局配置工作模式。
     */
    public static final String configWorkingMode =
        System.getProperty(CONFIG_WORKING_MODE) == null ?
            CONFIG_WORKING_MODE_GLOBAL :
            System.getProperty(CONFIG_WORKING_MODE);

    // **************** 私有变量

    /**
     * Log4j logger。
     */
    private static Logger logger = LogManager.getLogger(Config.class);

    /**
     * 全局唯一的配置信息对象。
     */
    private static CompositeConfiguration _configuration = null;

    /**
     * 全局唯一的配置信息对象锁。
     */
    private static final Object _configurationLock = new Object();

    /**
     * 本地线程配置信息对象。
     */
    private static final ThreadLocal<CompositeConfiguration>
        threadLocalConfiguration =
        ThreadLocal.withInitial(() -> newConfiguration());

    // **************** 继承方法

    // **************** 公开方法

    /**
     * 读取布尔型配置项值。
     *
     * @param key 配置项键。
     * @return 布尔型配置项值。
     * @defaultValue 默认值。
     */
    public static boolean getBoolean(String key, boolean defaultValue) {
        String booleanString;

        if ((booleanString = Config.get(key)) == null) {
            return defaultValue;
        }

        try {
            return Boolean.parseBoolean(booleanString);
        } catch (Exception e) {
            ConfigItemException cie =
                new ConfigItemException(key, "should " + "be a boolean.", e);
            logger.error(cie);
            throw cie;
        }
    }

    /**
     * 读取布尔型配置项值。
     *
     * @param key 配置项键。
     * @return 布尔型配置项值。
     */
    public static boolean getBoolean(String key) {
        String booleanString = getNotNull(key);

        try {
            return Boolean.parseBoolean(booleanString);
        } catch (Exception e) {
            ConfigItemException cie =
                new ConfigItemException(key, "should " + "be a boolean.", e);
            logger.error(cie);
            throw cie;
        }
    }

    /**
     * 获取用于读取的文件夹路径。
     *
     * @param key 配置项键。
     * @return 文件夹路径。
     */
    public static String getFolderForRead(String key) {
        String value = getFolder(key);

        if (!new File(value).exists()) {
            throw new ConfigItemException(key, "Folder does not exist.");
        }

        return value;
    }

    /**
     * 获取用于写入的文件夹路径。
     *
     * @param key 配置项键。
     * @return 文件夹路径。
     */
    public static String getFolderForWrite(String key) {
        String value = getFolder(key);

        File folder = new File(value);
        if (!folder.exists()) {
            folder.mkdirs();
        }

        return value;
    }

    /**
     * 获取文件夹路径。
     *
     * @param key 配置项键。
     * @return 文件夹路径。
     */
    public static String getFolder(String key) {
        String value = getNotNull(key);

        if (!value.endsWith("/")) {
            throw new ConfigItemException(key, "Folder should end with \"/\"");
        }

        return value;
    }

    /**
     * 获取用于读取的路径。
     *
     * @param key 配置项键。
     * @return 路径。
     */
    public static String getPathForRead(String key) {
        String value = getNotNull(key);

        if (!new File(value).exists()) {
            throw new ConfigItemException(key, "Path does not exist.");
        }

        return value;
    }

    /**
     * 获取用于读取的路经数组。
     *
     * @param key 配置项键。
     * @return 路经数组。
     */
    public static String[] getPathArrayForRead(String key) {
        String[] values = getStringArray(key);

        if (Arrays.stream(values).anyMatch(p -> !new File(p).exists())) {
            throw new ConfigItemException(key, "Paths do not exist: " + String
                .join(", ",
                    Arrays.stream(values).filter(p -> !new File(p).exists())
                        .toArray(String[]::new)));
        }

        return values;
    }

    /**
     * 获取用于写入的路径数组。
     *
     * @param key 配置项键。
     * @return 路经数组。
     */
    public static String[] getPathArrayForWrite(String key) {
        String[] values = getStringArray(key);

        if (Arrays.stream(values).anyMatch(p -> p.endsWith("/"))) {
            throw new ConfigItemException(key,
                "Path should not end with \"/\"");
        }

        File[] files =
            Arrays.stream(values).map(p -> new File(p)).toArray(File[]::new);
        File[] existingFiles =
            Arrays.stream(files).filter(p -> p.exists()).toArray(File[]::new);
        if (!Arrays.stream(existingFiles).anyMatch(p -> !p.isFile())) {
            throw new ConfigItemException(key,
                "Path already exists and is not a file.");
        }
        Arrays.stream(existingFiles).filter(p -> p.isFile())
            .forEach(p -> p.delete());

        Arrays.stream(files).map(p -> p.getParentFile())
            .filter(p -> !p.exists()).forEach(p -> p.mkdirs());

        return values;
    }

    /**
     * 获取用于写入的路径。
     *
     * @param key 配置项键。
     * @return 路径。
     */
    public static String getPathForWrite(String key) {
        String value = getNotNull(key);

        if (key.endsWith("/")) {
            throw new ConfigItemException(key,
                "Path should not end with \"/\"");
        }

        File file = new File(value);
        if (file.exists()) {
            if (file.isFile()) {
                file.delete();
            } else {
                throw new ConfigItemException(key,
                    "Path already exists and is not a file.");
            }
        }

        File folder = file.getParentFile();
        if (!folder.exists()) {
            folder.mkdirs();
        }

        return value;
    }


    /**
     * 获得长整数配置项值。
     *
     * @param key          配置项键。
     * @param defaultValue 默认值。
     * @return 长整数配置项值。
     */
    public static long getLong(String key, long defaultValue) {
        String longString;

        if ((longString = Config.get(key)) == null) {
            return defaultValue;
        }

        try {
            return Long.parseLong(longString);
        } catch (Exception e) {
            ConfigItemException cie =
                new ConfigItemException(key, "should " + "be a long.", e);
            logger.error(cie);
            throw cie;
        }
    }


    /**
     * 获得长整数配置项值。
     *
     * @param key 配置项键。
     * @return 长整数配置项值。
     */
    public static long getLong(String key) {
        String longString = getNotNull(key);

        try {
            return Long.parseLong(longString);
        } catch (Exception e) {
            ConfigItemException cie =
                new ConfigItemException(key, "should " + "be a long.", e);
            logger.error(cie);
            throw cie;
        }
    }

    /**
     * 获得浮点数配置项值
     *
     * @param key          配置项键。
     * @param defaultValue 默认值。
     * @return 浮点数配置项值。
     */
    public static double getDouble(String key, double defaultValue) {
        String doubleString = null;

        if ((doubleString = Config.get(key)) == null) {
            return defaultValue;
        }

        try {
            return Double.parseDouble(doubleString);
        } catch (Exception e) {
            ConfigItemException cie =
                new ConfigItemException(key, "should " + "be a double.", e);
            logger.error(cie);
            throw cie;
        }
    }

    /**
     * 获得浮点数配置项值。
     *
     * @param key 配置项键。
     * @return 浮点数配置项值。
     */
    public static double getDouble(String key) {
        String doubleString = getNotNull(key);

        try {
            return Double.parseDouble(doubleString);
        } catch (Exception e) {
            ConfigItemException cie =
                new ConfigItemException(key, "should " + "be a double.", e);
            logger.error(cie);
            throw cie;
        }
    }

    /**
     * 获得浮点数组配置项值。
     *
     * @param key 配置项键。
     * @return 浮点数组。
     */
    public static double[] getDoubleArray(String key) {
        String doubleArrayString = getNotNull(key);

        ArrayNode array;
        try {
            array = (ArrayNode) new ObjectMapper().readTree(doubleArrayString);
        } catch (Exception e) {
            ConfigItemException cie = new ConfigItemException(key,
                "should be" + " a json array of double.", e);
            logger.error(cie);
            throw cie;
        }

        return StreamSupport.stream(array.spliterator(), false)
            .map(DoubleNode.class::cast).mapToDouble(DoubleNode::asDouble)
            .toArray();
    }

    /**
     * 获得二维浮点数组配置项值。
     *
     * @param key 配置项键。
     * @return 二维浮点数组。
     */
    public static double[][] getDoubleArrays(String key) {
        String doubleArraysString = getNotNull(key);

        ArrayNode arrays;
        try {
            arrays =
                (ArrayNode) new ObjectMapper().readTree(doubleArraysString);
        } catch (Exception e) {
            ConfigItemException cie = new ConfigItemException(key,
                "should be" + " a json array of double array.", e);
            logger.error(cie);
            throw cie;
        }

        return StreamSupport.stream(arrays.spliterator(), false)
            .map(ArrayNode.class::cast).map(
                p -> StreamSupport.stream(p.spliterator(), false)
                    .map(DoubleNode.class::cast)
                    .mapToDouble(DoubleNode::asDouble).toArray())
            .collect(ArrayList<double[]>::new, ArrayList::add,
                ArrayList::addAll).toArray(new double[0][]);
    }

    /**
     * 获得整数配置项值。
     *
     * @param key          配置项键。
     * @param defaultValue 默认值。
     * @return 整数配置项值。
     */
    public static int getInt(String key, int defaultValue) {
        String intString = null;

        if ((intString = Config.get(key)) == null) {
            return defaultValue;
        }

        try {
            return Integer.parseInt(intString);
        } catch (Exception e) {
            ConfigItemException cie =
                new ConfigItemException(key, "should " + "be a int.", e);
            logger.error(cie);
            throw cie;
        }
    }

    /**
     * 获得整数配置项值。
     *
     * @param key 配置项键。
     * @return 整数配置项值。
     */
    public static int getInt(String key) {
        String intString = getNotNull(key);

        try {
            return Integer.parseInt(intString);
        } catch (Exception e) {
            ConfigItemException cie =
                new ConfigItemException(key, "should " + "be a int.", e);
            logger.error(cie);
            throw cie;
        }
    }

    /**
     * 获得整数数组配置项值。
     *
     * @param key 配置项键。
     * @return 整数数组。
     */
    public static int[] getIntArray(String key) {
        String intArrayString = getNotNull(key);

        ArrayNode array;
        try {
            array = (ArrayNode) new ObjectMapper().readTree(intArrayString);
        } catch (Exception e) {
            ConfigItemException cie = new ConfigItemException(key,
                "should be" + " a json array of int.", e);
            logger.error(cie);
            throw cie;
        }

        return StreamSupport.stream(array.spliterator(), false)
            .map(IntNode.class::cast).mapToInt(IntNode::asInt).toArray();
    }

    /**
     * 获得二维整数数组配置项值。
     *
     * @param key 配置项键。
     * @return 二维整数数组。
     */
    public static int[][] getIntArrays(String key) {
        String intArraysString = getNotNull(key);

        ArrayNode arrays;
        try {
            arrays = (ArrayNode) new ObjectMapper().readTree(intArraysString);
        } catch (Exception e) {
            ConfigItemException cie = new ConfigItemException(key,
                "should be" + " a json array of int array.", e);
            logger.error(cie);
            throw cie;
        }

        return StreamSupport.stream(arrays.spliterator(), false)
            .map(ArrayNode.class::cast).map(
                p -> StreamSupport.stream(p.spliterator(), false)
                    .map(IntNode.class::cast).mapToInt(IntNode::asInt)
                    .toArray())
            .collect(ArrayList<int[]>::new, ArrayList::add, ArrayList::addAll)
            .toArray(new int[0][]);
    }

    /**
     * 获得二维字符串数组配置项值。
     *
     * @param key 配置项键。
     * @return 二维字符串数组。
     */
    public static String[][] getStringArrays(String key) {
        String stringArraysString = getNotNull(key);

        ArrayNode arrays;
        try {
            arrays =
                (ArrayNode) new ObjectMapper().readTree(stringArraysString);
        } catch (Exception e) {
            ConfigItemException cie = new ConfigItemException(key,
                "should be" + " a json array of string array.", e);
            logger.error(e);
            throw cie;
        }

        return StreamSupport.stream(arrays.spliterator(), false)
            .map(ArrayNode.class::cast).map(
                p -> StreamSupport.stream(p.spliterator(), false)
                    .map(JsonNode::asText).toArray(String[]::new))
            .collect(ArrayList<String[]>::new, ArrayList::add,
                ArrayList::addAll).toArray(new String[0][]);
    }

    /**
     * 获得字符串数组配置项值。
     *
     * @param key 配置项键。
     * @return 字符串数组。
     */
    public static String[] getStringArray(String key) {
        String stringArrayString = getNotNull(key);

        ArrayNode array;
        try {
            array = (ArrayNode) new ObjectMapper().readTree(stringArrayString);
        } catch (Exception e) {
            ConfigItemException cie = new ConfigItemException(key,
                "should be" + " a json array of string.", e);
            logger.error(cie);
            throw cie;
        }

        return StreamSupport.stream(array.spliterator(), false)
            .map(JsonNode::asText).toArray(String[]::new);
    }

    /**
     * 获得时间戳配置项值。
     *
     * @param key 配置项键。
     * @return 时间戳。
     */
    public static Timestamp getTimestamp(String key) {
        String timestampString = getNotNull(key);

        try {
            SimpleDateFormat format = new SimpleDateFormat("yyyyMMddhhmmss");
            Date date = format.parse(timestampString);
            return new Timestamp(date.getTime());
        } catch (ParseException e) {
            ConfigItemException cie = new ConfigItemException(key,
                "should be" + " in format yyyyMMddhhmmss", e);
            logger.error(cie);
            throw cie;
        }
    }

    /**
     * 获得非空的配置项值。
     *
     * @param key 配置项键。
     * @return 配置项值。
     */
    public static String getNotNull(String key) {
        String value = get(key);

        if (value == null) {
            RuntimeException e = new MissingConfigItemException(key);
            logger.fatal(e.getMessage(), e);
            throw e;
        }

        return value;
    }

    /**
     * 获得配置项值。
     *
     * @param key          配置项键。
     * @param defaultValue 默认值。
     * @return 配置项值。
     */
    public static String get(String key, String defaultValue) {
        String value = get(key);
        return value != null ? value : defaultValue;
    }

    /**
     * 获得配置项值。
     *
     * @param key 配置项键。
     * @return 配置项值。
     */
    public static String get(String key) {
        return configuration().getString(key);
    }

    /**
     * 设置配置项键。
     *
     * @param key   配置项键。
     * @param value 配置项值。
     */
    public static void set(String key, String value) {
        configuration().setProperty(key, value);
    }

    // **************** 私有方法

    /**
     * 私有的构造函数。
     */
    private Config() {
    }

    /**
     * 获得配置信息对象。
     *
     * @return 配置信息对象。
     */
    private static CompositeConfiguration configuration() {
        switch (configWorkingMode) {
            case CONFIG_WORKING_MODE_THREADLOCAL:
                return threadLocalConfiguration.get();
            default:
                if (_configuration == null) {
                    synchronized (_configurationLock) {
                        if (_configuration == null) {
                            _configuration = newConfiguration();
                        }
                    }
                }
                return _configuration;
        }
    }

    /**
     * 获得新配置信息对象。
     *
     * @return 新配置信息对象。
     */
    public static CompositeConfiguration newConfiguration() {
        if (System.getProperty(CONFIG_FILE) == null ||
            "".equals(System.getProperty(CONFIG_FILE))) {
            RuntimeException e = new RuntimeException(
                "Specify config " + "file via -D" + CONFIG_FILE);
            logger.fatal(e.getMessage(), e);
            throw e;
        }

        CompositeConfiguration configuration = new CompositeConfiguration();
        try {
            configuration.addConfiguration(
                new PropertiesConfiguration(System.getProperty(CONFIG_FILE)));
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            throw new RuntimeException(e);
        }
        configuration.setDelimiterParsingDisabled(true);
        return configuration;
    }

}
