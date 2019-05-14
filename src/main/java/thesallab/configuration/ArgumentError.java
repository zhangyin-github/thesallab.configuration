package thesallab.configuration;


/**
 * 参数错误异常。
 *
 * @author Zhang, Yin
 */
@SuppressWarnings("serial")
public class ArgumentError extends RuntimeException {

    // **************** 公开变量

    // **************** 私有变量

    // **************** 继承方法

    // **************** 公开方法

    /**
     * 参数错误异常构造函数。
     *
     * @param cls 类名称。
     * @param arg 参数名称。
     * @param msg 错误信息。
     */
    public ArgumentError(Class cls, String arg, String msg) {
        super(String.format("Argument error %s#%s: %s",
                cls.getSimpleName(),
                arg,
                msg));
    }

    /**
     * 参数错误异常构造函数。
     *
     * @param cls 类名称。
     * @param mth 函数名称。
     * @param arg 参数名称。
     * @param msg 错误信息。
     */
    public ArgumentError(Class cls, String mth, String arg, String msg) {
        super(String.format("Argument error %s#%s(%s): %s",
                cls.getSimpleName(),
                mth,
                arg,
                msg));
    }

    // **************** 私有方法

}
