export interface HotItem {
  title: string
  link: string
  heat?: string
  desc?: string
}

export interface HotSection {
  name: string
  items: HotItem[]
}

