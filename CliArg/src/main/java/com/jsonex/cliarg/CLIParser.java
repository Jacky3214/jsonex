package com.jsonex.cliarg;

import com.jsonex.core.util.BeanConvertContext;
import com.jsonex.core.util.ClassUtil;
import com.jsonex.jsoncoder.JSONCoder;
import com.jsonex.jsoncoder.JSONCoderOption;
import com.jsonex.treedoc.TDNode.Type;
import com.jsonex.treedoc.json.TDJSONOption;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import java.util.*;

import static com.jsonex.core.util.LangUtil.doIf;
import static com.jsonex.core.util.ListUtil.isIn;

/**
 * Parse the input command line arguments against the {@link CLISpec}. The parsed result will be stored in the target
 *
 * @param <T>
 */
@RequiredArgsConstructor @Slf4j @Data
public class CLIParser<T> {
  final CLISpec<T> spec;

  final T target;
  final Set<String> missingParams;
  final String[] args;
  int argIndex;
  int paramIndex;
  List<String> extraArgs = new ArrayList<>();
  Map<String, String> errorMessages = new HashMap<>();

  @SneakyThrows
  public CLIParser(CLISpec descriptor, String[] args, int argIndex) {
    this(descriptor, args, argIndex, (T) descriptor.createDefaultInstance());
  }

  public CLIParser(CLISpec spec, String[] args, int argIndex, T target) {
    this.spec = spec;
    this.missingParams = new HashSet<>(spec.requiredParams);
    this.args = args;
    this.argIndex = argIndex;
    this.target = target;
  }

  public CLIParser<T> parse() {
    for (; argIndex < args.length; argIndex++) {
      String arg2 = argIndex < args.length - 1 ? args[argIndex + 1] : null;
      if (parse(args[argIndex], arg2)) {
        argIndex ++;
      }
    }
    return this;
  }

  /** @return true indicates it uses param2 */
  private boolean parse(String arg1, String arg2) {
    if (arg1.startsWith("--"))
      return parseOption(arg1.substring(2), arg2);
    else if (arg1.startsWith("-"))
      return parseOption(arg1.substring(1), arg2);
    parseArg(arg1);
    return false;
  }

  private boolean parseOption(String arg1, String arg2) {
    int p = arg1.indexOf('=');
    if (p >= 0) {
      parseNameValue(arg1.substring(0, p), arg1.substring(p+1));
      return false;
    }

    return parseNameValue(arg1, arg2);
  }

  private boolean parseNameValue(String name, String value) {
    Param param = spec.getOptionParamByName(name).orElse(null);
    if (param == null) {
      extraArgs.add(name);
      return false;
    }

    missingParams.remove(name);

    Class<?> type = param.property.getType();
    if (param.isBooleanType()) {
      if (isIn(value, "true", "false")) {  // Boolean type only accept "true", "false" parameters
        param.property.set(target, value.equals("true"));
        return true;
      }
      param.property.set(target, true);
      return false;
    }

    param.property.set(target, parseValue(param, value));
    return true;
  }

  private void parseArg(String arg) {
    if (paramIndex >= spec.indexedParams.size()) {
      extraArgs.add(arg);
      return;
    }
    Param param = spec.indexedParams.get(paramIndex++);
    missingParams.remove(param.name);
    param.property.set(target, parseValue(param, arg));
  }

  private Object parseValue(Param param, String value)  {
    Class<?> cls = param.property.getType();
    try {
      Object result = ClassUtil.stringToSimpleObject(value, cls, new BeanConvertContext());
      if (result != null)
        return result;

      Type rootType = cls.isArray() || Collection.class.isAssignableFrom(cls) ? Type.ARRAY : Type.MAP;
      return JSONCoder.decode(value, cls, JSONCoderOption.of().setJsonOption(TDJSONOption.ofDefaultRootType(rootType)));
    } catch (Exception e) {
      log.error("Error parsing parameter:" + param.name, e);
    }
    return null;
  }

  public boolean hasError() {
    return !missingParams.isEmpty() || !extraArgs.isEmpty() || !errorMessages.isEmpty();
  }

  public String getErrorsAsString() {
    StringBuilder sb = new StringBuilder();
    doIf(!missingParams.isEmpty(), () -> sb.append("\nMissing requied arguments:" + missingParams));
    doIf(!extraArgs.isEmpty(), () -> sb.append("\nUnexpected arguments:" + extraArgs));
    doIf(!errorMessages.isEmpty(), () -> sb.append("\nError parsing following arguments:" + errorMessages));
    return sb.toString();
  }
}
