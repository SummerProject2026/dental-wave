import { View, Text, TouchableOpacity, Image, StyleSheet } from 'react-native'
import { useRouter } from 'expo-router'
import ProfileAvatarButton from './profile-avatar'

type Props = {
  showBack?: boolean
  showMenu?: boolean
  hideProfile?: boolean
}

export default function AppHeader({ showBack = false, showMenu = false, hideProfile = false }: Props) {
  const router = useRouter()
  return (
    <View style={styles.header}>
      {showBack ? (
        <TouchableOpacity onPress={() => router.back()} style={styles.iconBtn}>
          <Text style={styles.icon}>←</Text>
        </TouchableOpacity>
      ) : showMenu ? (
        <TouchableOpacity style={styles.iconBtn}>
          <Text style={styles.icon}>☰</Text>
        </TouchableOpacity>
      ) : (
        <View style={styles.iconBtn} />
      )}

      <Image
        source={require('../../assets/images/wake-logo.png')}
        style={styles.logo}
        resizeMode="contain"
      />

      {hideProfile ? (
        <View style={styles.iconBtn} />
      ) : (
        <ProfileAvatarButton />
      )}
    </View>
  )
}

const styles = StyleSheet.create({
  header: {
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'space-between',
    paddingHorizontal: 16,
    paddingVertical: 8,
    backgroundColor: '#c8cbff',
  },
  iconBtn: {
    width: 36,
    alignItems: 'center',
  },
  icon: {
    fontSize: 26,
    color: '#1a1a2e',
  },
  logo: {
    width: 180,
    height: 60,
  },
})
