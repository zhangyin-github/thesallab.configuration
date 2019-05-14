package thesallab.configuration;

/**
 * 配置项异常。
 *
 * @author Zhang, Yin
 */
public class ConfigItemException extends RuntimeException {

    // **************** 公开变量

    // **************** 私有变量

    // **************** 继承方法

    // **************** 公开方法

    /**
     * 配置项异常构造函数。
     *
     * @param key 发生异常的配置项键。
     */
    public ConfigItemException(String key) {
        super(String.format("Error parsing config item %s", key));
    }

    /**
     * 配置项异常构造函数。
     *
     * @param key 发生异常的配置项键。
     * @param msg 异常信息。
     */
    public ConfigItemException(String key, String msg) {
        super(String.format("Error parsing config item %s: %s", key, msg));
    }

    /**
     * 配置项异常构造函数。
     *
     * @param key       发生异常的配置项键。
     * @param msg       异常信息。
     * @param throwable 异常原因。
     */
    public ConfigItemException(String key, String msg, Throwable throwable) {
        super(
                String.format("Error parsing config item %s: %s", key, msg),
                throwable);
    }

    // **************** 私有方法

}
