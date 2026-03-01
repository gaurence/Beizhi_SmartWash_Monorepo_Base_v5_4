package com.beizhi.smartwash.domain.service;

import com.beizhi.smartwash.domain.model.SlotState;
import com.beizhi.smartwash.interfaces.rest.error.ConflictException;
import org.springframework.stereotype.Component;

import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

@Component
public class SlotStateMachine {
  private final Map<SlotState, Set<SlotState>> transitions = new EnumMap<>(SlotState.class);

  public SlotStateMachine() {
    transitions.put(SlotState.RESERVED, EnumSet.of(SlotState.OCCUPIED, SlotState.FAULT, SlotState.LOCKED));
    transitions.put(SlotState.OCCUPIED, EnumSet.of(SlotState.RESERVED, SlotState.FAULT, SlotState.LOCKED));
    transitions.put(SlotState.FAULT, EnumSet.of(SlotState.RESERVED, SlotState.LOCKED));
    transitions.put(SlotState.LOCKED, EnumSet.of(SlotState.RESERVED, SlotState.FAULT));
  }

  public void assertTransitionAllowed(SlotState from, SlotState to) {
    Set<SlotState> allowed = transitions.getOrDefault(from, Set.of());
    if (!allowed.contains(to)) {
      throw new ConflictException("Illegal slot transition from " + from + " to " + to);
    }
  }

  public boolean canOpen(SlotState currentState) {
    return currentState == SlotState.RESERVED || currentState == SlotState.OCCUPIED;
  }
}
