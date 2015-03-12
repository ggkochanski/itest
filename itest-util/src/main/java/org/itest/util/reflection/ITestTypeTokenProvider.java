package org.itest.util.reflection;

import com.google.common.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.IdentityHashMap;
import java.util.Map;

/**
 * Created by rumcajs on 3/12/15.
 */
public class ITestTypeTokenProvider {
    private Map<Type,TypeToken> typeTypeTokenMap=new IdentityHashMap<Type, TypeToken>();
    public TypeToken getTypeToken(Type type){
        TypeToken res=typeTypeTokenMap.get(type);
        if(null==res){
            res=TypeToken.of(type);
            typeTypeTokenMap.put(type,res);
        }
        return res;
    }

}
