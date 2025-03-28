package sns.pinocchio.infrastructure.shared.util;

import com.github.f4b6a3.tsid.TsidCreator;

public class TsidUtil {

  public static String createTsid() {
    return TsidCreator.getTsid().toString();
  }
}
