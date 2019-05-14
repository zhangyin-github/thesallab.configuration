package thesallab.configuration;

/**
 * 缺少配置项异常。
 *
 * @author Zhang, Yin
 */
public class MissingConfigItemException extends RuntimeException {

    // **************** 公开变量

    // **************** 私有变量

    // **************** 继承方法

    // **************** 公开方法

    /**
     * 缺少配置项异常构造函数。
     *
     * @param key 配置项键。
     */
    public MissingConfigItemException(String key) {
        super(String.format("Missing config item %s", key));
    }

    // **************** 私有方法

}
