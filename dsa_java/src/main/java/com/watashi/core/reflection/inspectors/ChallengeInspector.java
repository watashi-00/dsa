package com.watashi.core.reflection.inspectors;

import com.watashi.core.reflection.metadata.Challenge;

public class ChallengeInspector implements Inspector{
    
    @Override 
    public void inspect(Class<?> clazz) {
        
        if(!clazz.isAnnotationPresent(Challenge.class)) {
            return;
        }
        
    }


}
