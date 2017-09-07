/**
 * Copyright (C) 2017 Lightbend Inc. <http://www.lightbend.com>
 */
package akka.typed.cluster

import akka.typed.scaladsl.adapter._
import akka.cluster.ClusterActorRefProvider
import akka.actor.DynamicAccess
import akka.actor.ActorSystem
import akka.event.EventStream
import akka.typed.ActorRef
import akka.remote.RemoteActorRef
import akka.actor.ExtensionIdProvider
import akka.actor.ExtensionId
import akka.actor.ExtendedActorSystem
import akka.actor.Extension

object ActorRefResolver extends ExtensionId[ActorRefResolver] with ExtensionIdProvider {
  override def get(system: ActorSystem): ActorRefResolver = super.get(system)

  override def lookup = ActorRefResolver

  override def createExtension(system: ExtendedActorSystem): ActorRefResolver =
    new ActorRefResolver(system)
}

class ActorRefResolver(system: ExtendedActorSystem) extends Extension {

  private def provider = system.provider.asInstanceOf[TypedClusterActorRefProvider]

  def toSerializationFormat[T](ref: ActorRef[T]): String =
    ref.path.toSerializationFormatWithAddress(provider.getDefaultAddress)

  def resolveActorRef[T](serializedActorRef: String): ActorRef[T] = {
    provider.resolveTypedActorRef(serializedActorRef)
  }
}

class TypedClusterActorRefProvider(
  _systemName:    String,
  _settings:      ActorSystem.Settings,
  _eventStream:   EventStream,
  _dynamicAccess: DynamicAccess)
  extends ClusterActorRefProvider(_systemName, _settings, _eventStream, _dynamicAccess) {

  def resolveTypedActorRef[T](path: String): ActorRef[T] = {
    super.resolveActorRef(path) match {
      case r: RemoteActorRef ⇒ new TypedRemoteActorRef[T](r, transport)
      case other             ⇒ other
    }
  }
}
