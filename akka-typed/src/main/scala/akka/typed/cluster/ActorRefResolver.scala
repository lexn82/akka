/**
 * Copyright (C) 2017 Lightbend Inc. <http://www.lightbend.com>
 */
package akka.typed.cluster

import akka.actor.ActorSystem
import akka.actor.ExtendedActorSystem
import akka.actor.Extension
import akka.actor.ExtensionId
import akka.actor.ExtensionIdProvider
import akka.typed.ActorRef

object ActorRefResolver extends ExtensionId[ActorRefResolver] with ExtensionIdProvider {
  override def get(system: ActorSystem): ActorRefResolver = super.get(system)

  override def lookup = ActorRefResolver

  override def createExtension(system: ExtendedActorSystem): ActorRefResolver =
    new ActorRefResolver(system)
}

class ActorRefResolver(system: ExtendedActorSystem) extends Extension {

  def toSerializationFormat[T](ref: ActorRef[T]): String =
    ref.path.toSerializationFormatWithAddress(system.provider.getDefaultAddress)

  def resolveActorRef[T](serializedActorRef: String): ActorRef[T] = {
    import akka.typed.scaladsl.adapter._
    system.provider.resolveActorRef(serializedActorRef)
  }
}

