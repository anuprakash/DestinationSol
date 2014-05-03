package com.miloshpetrov.sol2.game.planet;

import com.badlogic.gdx.math.Vector2;
import com.miloshpetrov.sol2.Const;
import com.miloshpetrov.sol2.common.Bound;
import com.miloshpetrov.sol2.common.SolMath;
import com.miloshpetrov.sol2.game.SolGame;

import java.util.ArrayList;
import java.util.List;

public class Planet {
  private final SolSystem mySys;
  private final Vector2 myPos;
  private final float myDist;
  private final float myToSysRotSpd;
  private final float myRotSpd;
  private final float myGroundHeight;
  private final PlanetConfig myConfig;
  private final float myGravConst;
  private final List<Vector2> myLps;
  private boolean myObjsCreated;

  private float myAngleToSys;
  private float myAngle;
  private float myMinGroundHeight;

  public Planet(SolSystem sys, float angleToSys, float dist, float angle, float toSysRotSpd, float rotSpd,
    float groundHeight, boolean objsCreated, PlanetConfig config) {
    mySys = sys;
    myAngleToSys = angleToSys;
    myDist = dist;
    myAngle = angle;
    myToSysRotSpd = toSysRotSpd;
    myRotSpd = rotSpd;
    myGroundHeight = groundHeight;
    myConfig = config;
    myMinGroundHeight = myGroundHeight;
    myObjsCreated = objsCreated;
    myPos = new Vector2();
    myGravConst = SolMath.rnd(config.minGrav, config.maxGrav) * myGroundHeight * myGroundHeight;
    myLps = new ArrayList<Vector2>();
    setSecondaryParams();
  }

  public void update(SolGame game) {
    float ts = game.getTimeStep();
    myAngleToSys += myToSysRotSpd * ts;
    myAngle += myRotSpd * ts;

    setSecondaryParams();
    Vector2 camPos = game.getCam().getPos();
    if (!myObjsCreated && camPos.dst(myPos) < getGroundHeight() + Const.MAX_SKY_HEIGHT_FROM_GROUND) {
      myMinGroundHeight = new PlanetObjsBuilder().createPlanetObjs(game, this);
      fillLangingPlaces(game);
      myObjsCreated = true;
    }
  }

  private void setSecondaryParams() {
    SolMath.fromAl(myPos, myAngleToSys, myDist, true);
    myPos.add(mySys.getPos());
  }

  private void fillLangingPlaces(SolGame game) {
    for (int i = 0; i < 10; i++) {
      Vector2 lp = game.getPlanetMan().findFlatPlace(game, this, null, 0);
      myLps.add(lp);
    }
  }

  public float getAngle() {
    return myAngle;
  }

  public Vector2 getPos() {
    return myPos;
  }

  public float getFullHeight() {
    return myGroundHeight + Const.ATM_HEIGHT;
  }

  public float getGroundHeight() {
    return myGroundHeight;
  }

  public SolSystem getSys() {
    return mySys;
  }

  @Bound
  public Vector2 getAdjustedEffectSpd(Vector2 pos, Vector2 spd) {
    Vector2 r = SolMath.getVec();
    Vector2 up = SolMath.distVec(myPos, pos);
    float dst = up.len();
    if (dst == 0 || getFullHeight() < dst) {
      r.set(spd);
      SolMath.free(up);
      return r;
    }
    float smokeConst = 1.2f * myGravConst;
    if (dst < myGroundHeight) {
      up.scl(smokeConst / dst / myGroundHeight / myGroundHeight);
      r.set(up);
      SolMath.free(up);
      return r;
    }
    r.set(spd);
    float spdPerc = (dst - myGroundHeight) / Const.ATM_HEIGHT;
    r.scl(spdPerc);
    up.scl(smokeConst / dst / dst / dst);
    r.add(up);
    SolMath.free(up);
    return r;
  }

  public float getGravConst() {
    return myGravConst;
  }

  public float getDist() {
    return myDist;
  }

  public float getAngleToSys() {
    return myAngleToSys;
  }

  public float getRotSpd() {
    return myRotSpd;
  }

  public boolean isObjsCreated() {
    return myObjsCreated;
  }

  public List<Vector2> getLandingPlaces() {
    return myLps;
  }

  public float getMinGroundHeight() {
    return myMinGroundHeight;
  }

  public boolean isNearGround(Vector2 pos) {
    return myPos.dst(pos) - myGroundHeight < .25f * Const.ATM_HEIGHT;
  }

  public PlanetConfig getConfig() {
    return myConfig;
  }

  public float getToSysRotSpd() {
    return myToSysRotSpd;
  }
}
