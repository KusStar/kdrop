import { decryptFile } from "@/utils/crypto";
import { encryptFile } from "@/utils/crypto";
import { type NextPage } from "next";
import Head from "next/head";
import Image from "next/image";
import { useRef, useState, Fragment, type HTMLAttributes, useEffect, type FC, useCallback } from "react";
import { Dialog, Transition } from '@headlessui/react'
import toast, { Toaster } from "react-hot-toast";

const API_URL = process.env.NODE_ENV === 'production' ? 'https://kdrop-api.uselessthing.top/api' : 'http://localhost:3000/api'

const Button = (props: HTMLAttributes<HTMLButtonElement>) => {
  return (<button
    type="button"
    className="bg-[#15162c] text-[#a7f974] px-4 py-2 rounded-md shadow-md hover:bg-[#a7f974] hover:text-[#15162c] transition duration-300 ease-in-out"
    {...props}
  >
  </button>
  )
}

const SuccessModal: FC<{
  visible: boolean
  onClose?: () => void
  downloadToken: string
}> = ({ visible, onClose, downloadToken }) => {
  const [isOpen, setIsOpen] = useState(visible)

  useEffect(() => {
    setIsOpen(visible)
  }, [visible])

  const _onClose = useCallback(() => {
    setIsOpen(false)
    onClose?.()
  }, [onClose])

  return (
    <>
      <Transition appear show={isOpen} as={Fragment}>
        <Dialog as="div" className="relative z-10" onClose={_onClose}>
          <Transition.Child
            as={Fragment}
            enter="ease-out duration-300"
            enterFrom="opacity-0"
            enterTo="opacity-100"
            leave="ease-in duration-200"
            leaveFrom="opacity-100"
            leaveTo="opacity-0"
          >
            <div className="fixed inset-0 bg-black bg-opacity-25" />
          </Transition.Child>

          <div className="fixed inset-0 overflow-y-auto">
            <div className="flex min-h-full items-center justify-center p-4 text-center">
              <Transition.Child
                as={Fragment}
                enter="ease-out duration-300"
                enterFrom="opacity-0 scale-95"
                enterTo="opacity-100 scale-100"
                leave="ease-in duration-200"
                leaveFrom="opacity-100 scale-100"
                leaveTo="opacity-0 scale-95"
              >
                <Dialog.Panel className="w-full max-w-md transform overflow-hidden rounded-2xl p-6 text-left align-middle shadow-xl transition-all bg-[#15162c]">
                  <Dialog.Title
                    as="h3"
                    className="text-2xl font-medium leading-6 text-[#a7f974]"
                  >
                    Your file has been uploaded!
                  </Dialog.Title>
                  <div className="mt-2">
                    <p className="text-lg text-gray-500">
                      The Download token is:
                    </p>
                    <p className="text-[#a7f974] m-4 rounded-lg p-4 text-center border-[#a7f974] border-2 text-xl">{downloadToken}</p>
                    <p className="text-gray-500">
                      You can share this token to let others download your encrypted file
                    </p>
                  </div>
                  <div className="mt-4">
                    <Button
                      onClick={_onClose}
                    >
                      Close
                    </Button>
                    <span className="m-2" />
                    <Button
                      onClick={async () => {
                        _onClose()

                        await navigator.clipboard.writeText(downloadToken)
                        toast.success('Successfully copied token!', {
                          style: {
                            color: '#96f364',
                            background: '#15162c',
                          },
                        })
                      }}
                    >
                      Copy token
                    </Button>
                  </div>
                </Dialog.Panel>
              </Transition.Child>
            </div>
          </div>
        </Dialog>
      </Transition>
    </>
  )
}

const Home: NextPage = () => {
  const [modalVisible, setModalVisible] = useState(false)
  const [downloadToken, setDownloadToken] = useState("")

  const uploadRef = useRef<HTMLInputElement>(null)

  const handleUpload = () => {
    uploadRef.current?.click()
  }

  const onFileChange = async (e: React.ChangeEvent<HTMLInputElement>) => {
    const file = e.target.files?.[0]
    if (!file) return

    const secret = prompt('Enter secret key')

    if (!secret) return

    const encryptedFile = await encryptFile(file, secret)

    const form = new FormData()
    form.append('file', encryptedFile)

    const res = await fetch(`${API_URL}/upload`, {
      method: 'POST',
      body: form
    })
    // eslint-disable-next-line @typescript-eslint/no-unsafe-assignment
    const json = (await res.json()) as { data: { secret: string } }

    if (typeof json !== 'object') return

    const token = json.data.secret

    setModalVisible(true)
    setDownloadToken(token)
  }

  const onDownload = async () => {
    const token = prompt('Enter download token')

    if (!token) return

    const res = await fetch(`${API_URL}/download/${token}`)

    const contentDisposition = res.headers.get('content-disposition')

    if (contentDisposition) {
      let filename = contentDisposition.split(';')
        .find(n => n.includes('filename='))
        ?.replace('filename=', '')
        .trim() || 'file'

      filename = decodeURI(filename)

      const blob = await res.blob()
      const file = new File([blob], filename, { type: blob.type });

      const secret = prompt('Enter secret key')

      const decryptedFile = await decryptFile(file, secret || 'secret')

      const url = window.URL.createObjectURL(decryptedFile)
      const link = document.createElement('a')
      link.href = url
      link.setAttribute('download', filename)
      document.body.appendChild(link)
      link.click()
      link.remove()
    }
  }

  return (
    <>
      <Head>
        <title>KDrop</title>
        <meta name="description" content="Generated by create-t3-app" />
        <link rel="icon" href="/logo.png" />
      </Head>
      <main className="flex min-h-screen flex-col items-center justify-center bg-gradient-to-b from-[#96f364] to-[#15162c]">
        <div className="container flex flex-col items-center justify-center gap-12 px-4 py-16 ">
          <Button
            onClick={handleUpload}
          >
            Upload
          </Button>
          <Button
            onClick={onDownload}
          >
            Download
          </Button>
          <input
            type="file"
            className="hidden"
            onChange={onFileChange}
            ref={uploadRef}
          />
          <Image
            src="/logo.png"
            alt="KDrop Logo"
            width={200}
            height={200}
            className="absolute bottom-12"
          />
        </div>
      </main>
      <SuccessModal visible={modalVisible} onClose={() => setModalVisible(false)} downloadToken={downloadToken} />
      <Toaster
        position="top-center"
        reverseOrder={false}
      />
    </>
  );
};

export default Home;
